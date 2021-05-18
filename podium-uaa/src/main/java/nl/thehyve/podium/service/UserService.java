/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.fasterxml.jackson.databind.*;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.config.UaaProperties;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.*;
import org.elasticsearch.search.builder.*;
import org.elasticsearch.search.suggest.*;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.*;
import java.time.LocalDateTime;
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
    private RestHighLevelClient elasticsearchClient;

    @Autowired
    private EntityManager entityManager;

    /**
     * Activate a user by a given key.
     * If the activation key has expired an exception is thrown.
     *
     * @param key The activation key.
     * @return true iff the verification was successful.
     * @throws VerificationKeyExpired Thrown when the used verification key has expired.
     */
    public boolean verifyRegistration(String key) throws VerificationKeyExpired {
        log.debug("Verifying user for activation key {}", key);

        Optional<User> foundUser = userRepository.findOneByDeletedIsFalseAndActivationKey(key);
        if (!foundUser.isPresent()) {
            return false;
        }
        Long activationKeyValidity = uaaProperties.getSecurity().getActivationKeyValiditySeconds();
        LocalDateTime keyValidPeriod = LocalDateTime.now().minusSeconds(activationKeyValidity);

        User user = foundUser.get();

        if (user.getActivationKeyDate().isBefore(keyValidPeriod)) {
            throw new VerificationKeyExpired();
        }
        // activate given user for the registration key.
        user.setEmailVerified(true);
        user.setActivationKey(null);
        user.setActivationKeyDate(null);
        user = save(user);

        UserRepresentation userRepresentation = userMapper.userToUserDTO(user);
        // Notify BBMRI admin
        Collection<ManagedUserRepresentation> administrators = this.getUsersByAuthority(AuthorityConstants.BBMRI_ADMIN);
        mailService.sendUserRegisteredEmail(administrators, userRepresentation);

        log.debug("Activated user: {}", user);
        return true;
    }

    public boolean renewVerificationKey(String key) {
        log.debug("Renewing activation key ", key);

        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndActivationKey(key)
            // Filter for expired activation keys
            .filter(user -> {
                Long activationKeyValidity = uaaProperties.getSecurity().getActivationKeyValiditySeconds();
                LocalDateTime keyValidPeriod = LocalDateTime.now().minusSeconds(activationKeyValidity);
                return user.getActivationKeyDate().isBefore(keyValidPeriod);
            });
        if (!userOptional.isPresent()) {
            return false;
        }
        User user = userOptional.get();
        user.setActivationKey(RandomUtil.generateActivationKey());
        user.setActivationKeyDate(LocalDateTime.now());
        user = save(user);
        UserRepresentation userRepresentation = userMapper.userToUserDTO(user);
        mailService.sendVerificationEmail(userRepresentation, user.getActivationKey());
        return true;
    }

    public boolean completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndResetKey(key)
            .filter(user -> {
                LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
                return user.getResetDate().isAfter(oneDayAgo);
            });
        if (!userOptional.isPresent()) {
            return false;
        }
        User user = userOptional.get();
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setActivationKey(null);
            user.setActivationKeyDate(null);

            SearchUser searchUser = userMapper.userToSearchUser(user);
            userSearchRepository.save(searchUser);
            log.debug("Activated user: {}", user);

            // Notify BBMRI admin
            UserRepresentation userRepresentation = userMapper.userToUserDTO(user);
            Collection<ManagedUserRepresentation> administrators = this.getUsersByAuthority(AuthorityConstants.BBMRI_ADMIN);
            mailService.sendUserRegisteredEmail(administrators, userRepresentation);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetKey(null);
        user.setResetDate(null);
        return true;
    }

    public void requestPasswordReset(String mail) {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndEmail(mail);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(LocalDateTime.now());
            ManagedUserRepresentation userVM = userMapper.userToManagedUserVM(user);
            mailService.sendPasswordResetMail(userVM, user.getResetKey());
        } else {
            mailService.sendPasswordResetMailNoUser(mail);
        }
    }

    /**
     * Check is the login and e-mail address that are being set are not already in use by another
     * user account.
     * Throws a {@link UserAccountException} if the e-mail address or login are already in use.
     *
     * @param updatedUserData the updated user account data.
     * @param userId          the id of the user to be updated. Can be {@code null} for new accounts.
     * @throws UserAccountException if the e-mail address or login are already in use.
     */
    private void checkForExistingLoginAndEmail(UserRepresentation updatedUserData, Long userId) throws UserAccountException {
        {
            Optional<ManagedUserRepresentation> existingAccount = getUserWithAuthoritiesByLogin(updatedUserData.getLogin().toLowerCase());
            if (existingAccount.isPresent()) {
                if (userId != null || existingAccount.get().getId().equals(userId)) {
                    // It's okay, we found the user we are updating
                } else {
                    throw new LoginAlreadyInUse("Login already in use");
                }
            }
        }
        {
            Optional<ManagedUserRepresentation> existingAccount = getUserWithAuthoritiesByEmail(updatedUserData.getEmail().toLowerCase());
            if (existingAccount.isPresent()) {
                if (userId != null || existingAccount.get().getId().equals(userId)) {
                    // It's okay, we found the user we are updating
                } else {
                    throw new EmailAddressAlreadyInUse("E-mail already in use");
                }
            }
        }
    }

    /**
     * Registers a new user account.
     *
     * @param managedUserRepresentation the user account details.
     * @throws UserAccountException if the username or email address is already in use.
     */
    public void registerUser(ManagedUserRepresentation managedUserRepresentation) throws UserAccountException {
        try {
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
            newUser.setActivationKeyDate(LocalDateTime.now());
            roles.add(role);
            newUser.setRoles(roles);
            newUser = save(newUser);

            ManagedUserRepresentation userRepresentation = userMapper.userToManagedUserVM(newUser);
            log.debug("Created Information for User: {}", userRepresentation);
            mailService.sendVerificationEmail(userRepresentation, newUser.getActivationKey());
        } catch (EmailAddressAlreadyInUse e) {
            Optional<ManagedUserRepresentation> userOptional = getUserWithAuthoritiesByEmail(managedUserRepresentation.getEmail());
            userOptional.ifPresent(user -> mailService.sendAccountAlreadyExists(user));
        } catch (LoginAlreadyInUse e) {
            log.error("Login already in use: {}", managedUserRepresentation.getLogin());
            throw e;
        }
    }

    public User createUser(UserRepresentation userData) throws UserAccountException {
        checkForExistingLoginAndEmail(userData, null);
        User user = new User();
        user.setLogin(userData.getLogin());
        user.setEmail(userData.getEmail());
        user = userMapper.safeUpdateUserWithUserDTO(userData, user);
        if (userData.getAuthorities() != null) {
            Set<Role> roles = new HashSet<>();
            userData.getAuthorities().forEach(authority -> {
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
        user.setResetDate(LocalDateTime.now());
        user.setEmailVerified(false);
        user.setAdminVerified(true);
        save(user);

        log.debug("Created Information for User: {}", user);
        return user;
    }

    public void createUserAccount(UserRepresentation userData) throws UserAccountException {
        try {
            User newUser = createUser(userData);
            UserRepresentation userRepresentation = userMapper.userToUserDTO(newUser);
            mailService.sendCreationEmail(userRepresentation, newUser.getResetKey());
        } catch (EmailAddressAlreadyInUse e) {
            Optional<ManagedUserRepresentation> userOptional = getUserWithAuthoritiesByEmail(userData.getEmail());
            userOptional.ifPresent(user -> mailService.sendAccountAlreadyExists(user));
        } catch (LoginAlreadyInUse e) {
            log.error("Login already in use: {}", userData.getLogin());
            throw e;
        }
    }

    /**
     * @param userData user data to update
     * @return Updated user data as UserRepresentation
     * @throws UserAccountException if login or email already in use.
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
        return userMapper.userToUserDTO(user);
    }

    public void updateUser(UserRepresentation userData) throws UserAccountException {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndUuid(userData.getUuid());
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User does not exists");
        }
        boolean userVerified = false;
        User user = userOptional.get();
        checkForExistingLoginAndEmail(userData, user.getId());
        user.setLogin(userData.getLogin());
        user.setEmail(userData.getEmail());
        if (userData.isAdminVerified() && !user.isAdminVerified()) {
            user.setAdminVerified(userData.isAdminVerified());
            userVerified = true;
        }
        Set<Role> managedRoles = user.getRoles();
        managedRoles.removeIf(role -> !role.getAuthority().isOrganisationAuthority());
        userData.getAuthorities().forEach(authority -> {
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
        user = save(user);
        ManagedUserRepresentation userRepresentation = userMapper.userToManagedUserVM(user);
        if (userVerified) {
            mailService.sendAccountVerifiedEmail(userRepresentation);
        }
        log.debug("Changed Information for User: {}", userRepresentation);
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
        });
    }

    public ManagedUserRepresentation unlockAccount(UUID uuid) {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndUuid(uuid);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found.");
        }
        User user = userOptional.get();
        entityManager.refresh(user);
        user.getAuthorities().size();

        user.setAccountLocked(false);
        user.setAccountLockDate(null);
        user.resetFailedLoginAttempts();
        user = save(user);
        return userMapper.userToManagedUserVM(user);
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

    public void deleteByLogin(String login) {
        Optional<User> userOptional = userRepository.findOneByDeletedIsFalseAndLogin(login);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found.");
        }
        User user = userOptional.get();
        user.setDeleted(true);
        save(user);
        log.debug("Deleted User: {}", user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getDomainUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByDeletedIsFalseAndLogin(login).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public Optional<ManagedUserRepresentation> getUserWithAuthoritiesByLogin(String login) {
        return getDomainUserWithAuthoritiesByLogin(login).map(user ->
            userMapper.userToManagedUserVM(user)
        );
    }

    @Transactional(readOnly = true)
    public Optional<User> getDomainUserByUuid(UUID uuid) {
        return userRepository.findOneByDeletedIsFalseAndUuid(uuid).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public Optional<ManagedUserRepresentation> getUserByUuid(UUID uuid) {
        return getDomainUserByUuid(uuid).map(user ->
            userMapper.userToManagedUserVM(user)
        );
    }

    @Transactional(readOnly = true)
    public List<ManagedUserRepresentation> getUsersByAuthority(String authority) {
        return userMapper.usersToManagedUserVMs(userRepository.findAllByDeletedIsFalseAndAuthority(authority));
    }

    @Transactional(readOnly = true)
    public ManagedUserRepresentation getUserWithAuthorities(Long id) {
        User user = userRepository.findById(id).get();
        entityManager.refresh(user);
        user.getAuthorities().size(); // eagerly load the association
        return userMapper.userToManagedUserVM(user);
    }

    @Transactional(readOnly = true)
    public UserRepresentation getUserWithAuthorities() {
        String login = SecurityService.getCurrentUserLogin();
        log.debug("Fetching user with login {}", login);
        Optional<User> optionalUser = userRepository.findOneByDeletedIsFalseAndLogin(login);
        User user = null;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            entityManager.refresh(user);
            user.getAuthorities().size(); // eagerly load the association
        }
        return userMapper.userToUserDTO(user);
    }

    @Transactional(readOnly = true)
    public Optional<ManagedUserRepresentation> getUserWithAuthoritiesByEmail(String email) {
        return userRepository.findOneByDeletedIsFalseAndEmail(email).map(user -> {
            entityManager.refresh(user);
            user.getAuthorities().size();
            return userMapper.userToManagedUserVM(user);
        });
    }

    @Transactional(readOnly = true)
    public Page<ManagedUserRepresentation> getUsers(Pageable pageable) {
        return userRepository.findAllWithAuthorities(pageable)
            .map(user -> userMapper.userToManagedUserVM(user));
    }

    /**
     * Fetch users that are associated with an organisation role for any of the organisations
     * with uuid in organisationUuids.
     *
     * @param pageable          pagination information.
     * @param organisationUuids the uuids of the organisations to fetch the users for.
     * @return a page with users.
     */
    @Transactional(readOnly = true)
    public Page<ManagedUserRepresentation> getUsersForOrganisations(Pageable pageable, UUID... organisationUuids) {
        return userRepository.findAllByOrganisations(Arrays.asList(organisationUuids), pageable)
            .map(user -> userMapper.userToManagedUserVM(user));
    }

    /**
     * Search for the organisation corresponding to the query.
     *
     * @param query the query of the search
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<SearchUser> search(String query) {
        log.debug("Request to search users for query {}", query);
        return StreamSupport
            .stream(userSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchUser> suggestUsers(String query) throws IOException {
        CompletionSuggestionBuilder completionSuggestionBuilder
            = new CompletionSuggestionBuilder("fullNameSuggest")
            .text(query);
        SuggestBuilder suggest = new SuggestBuilder().addSuggestion("fullname-suggest", completionSuggestionBuilder);
        SearchRequest searchRequest = new SearchRequest("searchuser")
            .source(new SearchSourceBuilder().suggest(suggest));
        CompletionSuggestion completionSuggestion = elasticsearchClient.search(searchRequest, RequestOptions.DEFAULT)
            .getSuggest().getSuggestion("fullname-suggest");
        List<CompletionSuggestion.Entry.Option> options = completionSuggestion.getEntries().get(0).getOptions();
        return userMapper.completionSuggestOptionsToSearchUsers(options);
    }
}
