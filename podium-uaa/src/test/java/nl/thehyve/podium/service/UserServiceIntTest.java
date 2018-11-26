/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.repository.UserRepository;
import nl.thehyve.podium.service.util.RandomUtil;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
@Transactional
public class UserServiceIntTest {

    private static final String VALID_PASSWORD = "johndoe2!";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    static ManagedUserRepresentation createTestUser() {
        ManagedUserRepresentation userVM = new ManagedUserRepresentation();
        userVM.setLogin("johndoe");
        userVM.setLogin("johndoes");
        userVM.setFirstName("John");
        userVM.setLastName("Doe");
        userVM.setEmail("john.doe@localhost");
        userVM.setLangKey("en");
        return userVM;
    }

    @Test
    public void assertThatResetKeyMustNotBeOlderThan24Hours() throws UserAccountException {
        ManagedUserRepresentation testUserData = createTestUser();
        User user = userService.createUser(testUserData);

        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        String resetKey = RandomUtil.generateResetKey();
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);

        userRepository.save(user);

        boolean completed = userService.completePasswordReset(VALID_PASSWORD, user.getResetKey());

        assertThat(completed).isFalse();

        userRepository.delete(user);
    }

    @Test
    public void assertThatResetKeyMustBeValid() throws UserAccountException {
        ManagedUserRepresentation testUserData = createTestUser();
        User user = userService.createUser(testUserData);

        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(25);
        user.setResetDate(daysAgo);
        user.setResetKey("1234");
        userRepository.save(user);
        boolean completed = userService.completePasswordReset(VALID_PASSWORD, user.getResetKey());
        assertThat(completed).isFalse();
        userRepository.delete(user);
    }

    @Test
    public void assertThatUserCanResetPassword() throws UserAccountException {
        ManagedUserRepresentation testUserData = createTestUser();
        User user = userService.createUser(testUserData);
        String oldPassword = user.getPassword();
        ZonedDateTime daysAgo = ZonedDateTime.now().minusHours(2);
        String resetKey = RandomUtil.generateResetKey();
        user.setResetDate(daysAgo);
        user.setResetKey(resetKey);
        userRepository.save(user);
        boolean completed = userService.completePasswordReset(VALID_PASSWORD, user.getResetKey());
        assertThat(completed).isTrue();
        User domainUser = userService.getDomainUserByUuid(user.getUuid()).get();
        assertThat(domainUser.getResetDate()).isNull();
        assertThat(domainUser.getResetKey()).isNull();
        assertThat(domainUser.getPassword()).isNotEqualTo(oldPassword);

        userRepository.delete(user);
    }

}
