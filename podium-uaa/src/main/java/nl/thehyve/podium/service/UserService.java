/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.event.AuthenticationEvent;
import nl.thehyve.podium.common.event.EventType;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.config.UaaProperties;
import nl.thehyve.podium.domain.PersistentAuditEvent;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.repository.PersistenceAuditEventRepository;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.mapstruct.ap.shaded.freemarker.template.utility.SecurityUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UaaProperties uaaProperties;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    /**
     * Activate a user by a given key.
     * If the activation key has expired return null
     *
     * @param key The activation key.
     * @throws VerificationKeyExpired Thrown when the used verification key has expired.
     *
     * @return the user
     */
    public Optional<User> verifyRegistration(String key) throws VerificationKeyExpired {
        log.debug("Verifying user for activation key {}", key);

        Optional<User> foundUser = userRepository.findOneByDeletedIsFalseAndActivationKey(key);
        Long activationKeyValidity = uaaProperties.getSecurity().getActivationKeyValiditySeconds();
        ZonedDateTime keyValidPeriod = ZonedDateTime.now().minusSeconds(activationKeyValidity);

        if (foundUser.isPresent()) {
            User user = foundUser.get();

            if(user.getActivationKeyDate().isBefore(keyValidPeriod)) {
                throw new VerificationKeyExpired();
            }
            // activate given user for the registration key.
            user.setEmailVerified(true);
            user.setActivationKey(null);
            user.setActivationKeyDate(null);

            save(user);

            // Notify BBMRI admin
            Collection<User> administrators = this.getUsersByAuthority(AuthorityConstants.BBMRI_ADMIN);
            mailService.sendUserRegisteredEmail(administrators, user);

            log.debug("Activated user: {}", user);
            AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Verified_Registration, user.getUserUuid());
            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }

        return foundUser;
    }

    public Optional<User> renewVerificationKey(String key) {
        log.debug("Renewing activation key ", key);

        return userRepository.findOneByDeletedIsFalseAndActivationKey(key)
            // Filter for expired activation keys
            .filter(user -> {
                Long activationKeyValidity = uaaProperties.getSecurity().getActivationKeyValiditySeconds();
                ZonedDateTime keyValidPeriod = ZonedDateTime.now().minusSeconds(activationKeyValidity);
                return user.getActivationKeyDate().isBefore(keyValidPeriod);
            })
            .map(user -> {
                user.setActivationKey(RandomUtil.generateActivationKey());
                user.setActivationKeyDate(ZonedDateTime.now());
                save(user);
                mailService.sendVerificationEmail(user);
                AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Send_ActivationKey, user.getUserUuid());
                PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
                persistenceAuditEventRepository.save(persistentAuditEvent);
                return user;
            });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
       log.debug("Reset user password for reset key {}", key);
       return userRepository.findOneByDeletedIsFalseAndResetKey(key)
            .filter(user -> {
                ZonedDateTime oneDayAgo = ZonedDateTime.now().minusHours(24);
                return user.getResetDate().isAfter(oneDayAgo);
           })
           .map(user -> {
                if (!user.isEmailVerified()) {
                    user.setEmailVerified(true);
                    user.setActivationKey(null);
                    user.setActivationKeyDate(null);

                    SearchUser searchUser = userMapper.userToSearchUser(user);
                    userSearchRepository.save(searchUser);
                    log.debug("Activated user: {}", user);
                    AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.User_Activated, user.getUserUuid());
                    PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
                    persistenceAuditEventRepository.save(persistentAuditEvent);

                    // Notify BBMRI admin
                    Collection<User> administrators = this.getUsersByAuthority(AuthorityConstants.BBMRI_ADMIN);
                    mailService.sendUserRegisteredEmail(administrators, user);
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Update_UserPassword, user.getUserUuid());
                PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
                persistenceAuditEventRepository.save(persistentAuditEvent);
                return user;
           });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByDeletedIsFalseAndEmail(mail)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Reset_UserPassword_Request, user.getUserUuid());
                PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
                persistenceAuditEventRepository.save(persistentAuditEvent);
                return user;
            });
    }

    /**
     * Check is the login and e-mail address that are being set are not already in use by another
     * user account.
     * Throws a {@link UserAccountException} if the e-mail address or login are already in use.
     *
     * @param updatedUserData the updated user account data.
     * @param userId the id of the user to be updated. Can be {@code null} for new accounts.
     * @throws UserAccountException if the e-mail address or login are already in use.
     */
    private void checkForExistingLoginAndEmail(UserRepresentation updatedUserData, Long userId) throws UserAccountException {
        {
            Optional<User> existingAccount = getUserWithAuthoritiesByLogin(updatedUserData.getLogin().toLowerCase());
            if (existingAccount.isPresent()) {
                if (userId != null || existingAccount.get().getId().equals(userId)) {
                    // It's okay, we found the user we are updating
                } else {
                    throw new LoginAlreadyInUse("Login already in use");
                }
            }
        }
        {
            Optional<User> existingAccount = getUserWithAuthoritiesByEmail(updatedUserData.getEmail().toLowerCase());
            if (existingAccount.isPresent()) {
                if (userId != null || existingAccount.get().getId().equals(userId)) {
                    // It's okay, we found the user we are updating
                } else {
                    throw new EmailAddressAlreadyInUse("E-mail already in use");
                }
            }
        }
    }

    public User registerUser(ManagedUserRepresentation managedUserRepresentation) throws UserAccountException {
        checkForExistingLoginAndEmail(managedUserRepresentation, null);
        User newUser = new User();
        Role role = roleService.findRoleByAuthorityName(AuthorityConstants.RESEARCHER);
        Set<Role> roles = new HashSet<>();
        newUser.setLogin(managedUserRepresentation.getLogin());
        newUser.setEmail(managedUserRepresentation.getEmail());
        String encryptedPassword = passwordEncoder.encode(managedUserRepresentation.getPassword());
        newUser.setPassword(encryptedPassword);
        newUser = userMapper.safeUpdateUserWithUserDTO(managedUserRepresentation, newUser);
        // new user is not active
        newUser.setEmailVerified(false);
        newUser.setAdminVerified(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        newUser.setActivationKeyDate(ZonedDateTime.now());
        roles.add(role);
        newUser.setRoles(roles);
        save(newUser);

        log.debug("Created Information for User: {}", newUser);
        AuthenticationEvent authenticationEvent = new AuthenticationEvent(newUser, EventType.Registration, newUser.getUserUuid());
        PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
        persistenceAuditEventRepository.save(persistentAuditEvent);
        return newUser;
    }

    public User createUser(UserRepresentation userData) throws UserAccountException {
        checkForExistingLoginAndEmail(userData, null);
        User user = new User();
        user.setLogin(userData.getLogin());
        user.setEmail(userData.getEmail());
        user = userMapper.safeUpdateUserWithUserDTO(userData, user);
        if (userData.getAuthorities() != null) {
            Set<Role> roles = new HashSet<>();
            userData.getAuthorities().forEach( authority -> {
                Role role = roleService.findRoleByAuthorityName(authority);
                if (role != null) {
                    roles.add(role);
                }
            });
            user.setRoles(roles);
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(ZonedDateTime.now());
        user.setEmailVerified(false);
        user.setAdminVerified(true);
        save(user);

        log.debug("Created Information for User: {}", user);
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin());
        AuthenticationEvent authenticationEvent;
        if (userOptional.isPresent()) {
            User handler = userOptional.get();
            authenticationEvent = new AuthenticationEvent(user, EventType.Registration, user.getUserUuid(), handler.getUserUuid());
        } else {
            authenticationEvent = new AuthenticationEvent(user, EventType.Registration, user.getUserUuid());
        }

        PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
        persistenceAuditEventRepository.save(persistentAuditEvent);
        return user;
    }

    /**
     *
     * @param userData user data to update
     * @return Updated user data as UserRepresentation
     * @throws UserAccountException
     */
    public UserRepresentation updateUserAccount(UserRepresentation userData) throws UserAccountException {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin());
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("Account does not exists");
        }
        User user = userOptional.get();
        checkForExistingLoginAndEmail(userData, user.getId());
        user = userMapper.safeUpdateUserWithUserDTO(userData, user);
        user = save(user);

        log.debug("Changed Information for User: {}", user);
        AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Update_User, user.getUserUuid());
        PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
        persistenceAuditEventRepository.save(persistentAuditEvent);
        return userMapper.userToUserDTO(user);
    }

    public void updateUser(UserRepresentation userData) throws UserAccountException {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndUuid(userData.getUuid());
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User does not exists");
        }
        User user = userOptional.get();
        checkForExistingLoginAndEmail(userData, user.getId());
        user.setLogin(userData.getLogin());
        user.setEmail(userData.getEmail());
        user.setAdminVerified(userData.isAdminVerified());
        Set<Role> managedRoles = user.getRoles();
        managedRoles.removeIf(role -> !role.getAuthority().isOrganisationAuthority());
        userData.getAuthorities().forEach( authority -> {
            if (!AuthorityConstants.isOrganisationAuthority(authority)) {
                log.info("Adding role: {}", authority);
                Role role = roleService.findRoleByAuthorityName(authority);
                if (role != null) {
                    managedRoles.add(role);
                } else {
                    log.error("Could not find role: {}", authority);
                }
            }
        });

        user = userMapper.safeUpdateUserWithUserDTO(userData, user);
        save(user);
        log.debug("Changed Information for User: {}", user);
    }

    @Profile({"dev", "test"})
    public void changePassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        log.debug("Changed password for User: {}", user);
        save(user);
    }

    public void changePassword(String password) {
        userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
            AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Update_UserPassword, user.getUserUuid());
            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
            persistenceAuditEventRepository.save(persistentAuditEvent);
        });
    }

    public User unlockAccount(User user) {
        user.setAccountLocked(false);
        user.setAccountLockDate(null);
        user.resetFailedLoginAttempts();
        AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Unlock_Account, user.getUserUuid());
        PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
        persistenceAuditEventRepository.save(persistentAuditEvent);
        return save(user);
    }

    /**
     * Save or update the user entity in the db as well as the searchUser in Elasticsearch
     *
     * @param user the user to save or update
     * @return User the saved user
     */
    public User save(User user) {
        User updatedUser = userRepository.save(user);
        SearchUser searchUser = userMapper.userToSearchUser(updatedUser);
        userSearchRepository.save(searchUser);
        return updatedUser;
    }

    public void delete(User user) {
        user.setDeleted(true);
        save(user);

        log.debug("Deleted User: {}", user);
        AuthenticationEvent authenticationEvent = new AuthenticationEvent(user, EventType.Delete_User, user.getUserUuid());
        PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent(authenticationEvent);
        persistenceAuditEventRepository.save(persistentAuditEvent);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByDeletedIsFalseAndLogin(login).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUuid(UUID uuid) {
        return userRepository.findOneByDeletedIsFalseAndUuid(uuid).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByAuthority(String authority) {
        return userRepository.findAllByDeletedIsFalseAndAuthority(authority);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        User user = userRepository.findOne(id);
        entityManager.refresh(user);
        user.getAuthorities().size(); // eagerly load the association
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        String login = SecurityService.getCurrentUserLogin();
        log.debug("Fetching user with login {}", login);
        Optional<User> optionalUser = userRepository.findOneByDeletedIsFalseAndLogin(login);
        User user = null;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            entityManager.refresh(user);
            user.getAuthorities().size(); // eagerly load the association
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByEmail(String email) {
        return userRepository.findOneByDeletedIsFalseAndEmail(email).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAllWithAuthorities(pageable);
    }

    /**
     * Fetch users that are associated with an organisation role for any of the organisations
     * with uuid in organisationUuids.
     *
     * @param pageable pagination information.
     * @param organisationUuids the uuids of the organisations to fetch the users for.
     * @return a page with users.
     */
    @Transactional(readOnly = true)
    public Page<User> getUsersForOrganisations(Pageable pageable, UUID ... organisationUuids) {
        return userRepository.findAllByOrganisations(Arrays.asList(organisationUuids), pageable);
    }

    /**
     * Search for the organisation corresponding to the query.
     *
     *  @param query the query of the search
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SearchUser> search(String query) {
        log.debug("Request to search users for query {}", query);
        return StreamSupport
            .stream(userSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchUser> suggestUsers(String query) {

        CompletionSuggestionBuilder completionSuggestionBuilder
            = new CompletionSuggestionBuilder("fullname-suggest")
            .text(query)
            .field("fullNameSuggest");

        SuggestResponse suggestResponse = elasticsearchTemplate.suggest(completionSuggestionBuilder, SearchUser.class);
        CompletionSuggestion completionSuggestion = suggestResponse.getSuggest().getSuggestion("fullname-suggest");
        List<CompletionSuggestion.Entry.Option> options = completionSuggestion.getEntries().get(0).getOptions();

        List<SearchUser> suggestedUsers = userMapper.completionSuggestOptionsToSearchUsers(options);

        return suggestedUsers;
    }
}
