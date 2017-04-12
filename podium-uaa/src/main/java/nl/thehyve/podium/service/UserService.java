/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.security.SecurityService;
import nl.thehyve.podium.config.UaaProperties;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import javax.inject.Inject;
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

    @Inject
    private PasswordEncoder passwordEncoder;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserSearchRepository userSearchRepository;

    @Inject
    private RoleService roleService;

    @Autowired
    private UaaProperties uaaProperties;

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

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

            SearchUser searchUser = userMapper.userToSearchUser(user);
            userSearchRepository.save(searchUser);

            save(user);
            log.debug("Activated user: {}", user);
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
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setResetKey(null);
                user.setResetDate(null);
                return user;
           });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByDeletedIsFalseAndEmail(mail)
            .filter(User::isActivated)
            .map(user -> {
                user.setResetKey(RandomUtil.generateResetKey());
                user.setResetDate(ZonedDateTime.now());
                return user;
            });
    }

    /**
     * Copy user properties, except login, password, email, activated.
     * @param source
     * @param target
     */
    private void copyProperties(UserRepresentation source, User target) {
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setLangKey(source.getLangKey());
        // update language key if set in source, or set default if not set in target.
        if (source.getLangKey() != null) {
            target.setLangKey(source.getLangKey());
        } else if (target.getLangKey() == null) {
            target.setLangKey("en"); // default language
        }
        target.setTelephone(source.getTelephone());
        target.setInstitute(source.getInstitute());
        target.setDepartment(source.getDepartment());
        target.setJobTitle(source.getJobTitle());
        target.setSpecialism(source.getSpecialism());
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

    public User registerUser(ManagedUserVM managedUserVM) throws UserAccountException {
        checkForExistingLoginAndEmail(managedUserVM, null);
        User newUser = new User();
        Role role = roleService.findRoleByAuthorityName(AuthorityConstants.RESEARCHER);
        Set<Role> roles = new HashSet<>();
        newUser.setLogin(managedUserVM.getLogin());
        newUser.setEmail(managedUserVM.getEmail());
        String encryptedPassword = passwordEncoder.encode(managedUserVM.getPassword());
        newUser.setPassword(encryptedPassword);
        copyProperties(managedUserVM, newUser);
        // new user is not active
        newUser.setEmailVerified(false);
        newUser.setAdminVerified(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        newUser.setActivationKeyDate(ZonedDateTime.now());
        roles.add(role);
        newUser.setRoles(roles);
        save(newUser);

        SearchUser searchUser = userMapper.userToSearchUser(newUser);
        userSearchRepository.save(searchUser);

        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(UserRepresentation userData) throws UserAccountException {
        checkForExistingLoginAndEmail(userData, null);
        User user = new User();
        user.setLogin(userData.getLogin());
        user.setEmail(userData.getEmail());
        copyProperties(userData, user);
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

        SearchUser searchUser = userMapper.userToSearchUser(user);
        userSearchRepository.save(searchUser);

        log.debug("Created Information for User: {}", user);
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
        copyProperties(userData, user);
        user = save(user);

        SearchUser searchUser = userMapper.userToSearchUser(user);
        userSearchRepository.save(searchUser);
        log.debug("Changed Information for User: {}", user);
        // FIXME: Add mapstruct mapper for all entity representations
        return user.toRepresentation();
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

        copyProperties(userData, user);
        save(user);
        log.debug("Changed Information for User: {}", user);
    }

    public void changePassword(String password) {
        userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            log.debug("Changed password for User: {}", user);
        });
    }

    public User unlockAccount(User user) {
        user.setAccountLocked(false);
        user.setAccountLockDate(null);
        user.resetFailedLoginAttempts();
        return save(user);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        user.setDeleted(true);
        save(user);

        SearchUser searchUser = userMapper.userToSearchUser(user);
        userSearchRepository.save(searchUser);

        log.debug("Deleted User: {}", user);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneByDeletedIsFalseAndLogin(login).map(user -> {
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUuid(UUID uuid) {
        return userRepository.findOneByDeletedIsFalseAndUuid(uuid).map(user -> {
            user.getAuthorities().size();
            return user;
        });
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        User user = userRepository.findOne(id);
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
            user.getAuthorities().size(); // eagerly load the association
        }
        return user;
    }

    public Optional<User> getUserWithAuthoritiesByEmail(String email) {
        return userRepository.findOneByDeletedIsFalseAndEmail(email).map(user -> {
            user.getAuthorities().size();
            return user;
        });
    }

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAllWithAuthorities(pageable);
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
