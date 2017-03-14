/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.domain.Role;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.security.SecurityService;
import nl.thehyve.podium.config.UaaProperties;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.service.representation.UserRepresentation;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import javax.inject.Inject;
import java.util.*;

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

    /**
     * Activate a user by a given key.
     * If the activation key has expired return null
     *
     * @param key The activation key
     * @throws VerificationKeyExpired
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
            userSearchRepository.save(user);
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
                    userSearchRepository.save(user);
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
        target.setAdminVerified(source.isAdminVerified());
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
    private void checkForExistingLoginAndEmail(ManagedUserVM updatedUserData, Long userId) throws UserAccountException {
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
        userSearchRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    public User createUser(ManagedUserVM managedUserVM) throws UserAccountException {
        checkForExistingLoginAndEmail(managedUserVM, null);
        User user = new User();
        user.setLogin(managedUserVM.getLogin());
        user.setEmail(managedUserVM.getEmail());
        copyProperties(managedUserVM, user);
        if (managedUserVM.getAuthorities() != null) {
            Set<Role> roles = new HashSet<>();
            managedUserVM.getAuthorities().forEach( authority -> {
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
        save(user);
        userSearchRepository.save(user);
        log.debug("Created Information for User: {}", user);
        return user;
    }

    public void updateUserAccount(UserRepresentation userData) {
        userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin()).ifPresent(user -> {
            copyProperties(userData, user);
            userSearchRepository.save(user);
            log.debug("Changed Information for User: {}", user);
        });
    }

    public void updateUser(ManagedUserVM managedUserVM) throws UserAccountException {
        User user = userRepository.findOne(managedUserVM.getId());
        if (user == null) {
           return;
        }
        checkForExistingLoginAndEmail(managedUserVM, user.getId());
        user.setLogin(managedUserVM.getLogin());
        user.setEmail(managedUserVM.getEmail());
        user.setAdminVerified(managedUserVM.isAdminVerified());
        Set<Role> managedRoles = user.getRoles();
        managedRoles.removeIf(role -> !role.getAuthority().isOrganisationAuthority());
        managedUserVM.getAuthorities().forEach( authority -> {
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
        copyProperties(managedUserVM, user);
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
        userSearchRepository.delete(user);
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
        Optional<User> optionalUser = userRepository.findOneByDeletedIsFalseAndLogin(SecurityService.getCurrentUserLogin());
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
}
