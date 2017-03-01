/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.service;

import org.bbmri.podium.domain.Role;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.repository.UserRepository;
import org.bbmri.podium.repository.search.UserSearchRepository;
import org.bbmri.podium.common.security.AuthorityConstants;
import org.bbmri.podium.security.SecurityService;
import org.bbmri.podium.service.representation.UserRepresentation;
import org.bbmri.podium.service.util.RandomUtil;
import org.bbmri.podium.web.rest.vm.ManagedUserVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByDeletedIsFalseAndActivationKey(key)
            .filter(user -> {
                ZonedDateTime oneWeekAgo = ZonedDateTime.now().minusWeeks(1);
                return user.getActivationKeyDate().isAfter(oneWeekAgo);
            })
            .map(user -> {
                // activate given user for the registration key.
                user.setEmailVerified(true);
                user.setActivationKey(null);
                user.setActivationKeyDate(null);
                userSearchRepository.save(user);
                log.debug("Activated user: {}", user);
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
    }

    public User registerUser(ManagedUserVM managedUserVM) {
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

    public User createUser(ManagedUserVM managedUserVM) {
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

    public void updateUser(ManagedUserVM managedUserVM) {
       Optional.of(userRepository
            .findOne(managedUserVM.getId()))
            .ifPresent(user -> {
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
            });
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
