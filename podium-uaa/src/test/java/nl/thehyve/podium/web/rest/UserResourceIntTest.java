/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class UserResourceIntTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    /**
     * Create a User.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static User createEntity(EntityManager em) {
        User user = new User();
        user.setLogin("test");
        user.setPassword(RandomStringUtils.random(60));
        user.setEmail("test@test.com");
        user.setFirstName("test");
        user.setLastName("test");
        user.setLangKey("en");
        em.persist(user);
        em.flush();
        return user;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendVerificationEmail((User) anyObject());

        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);
        ReflectionTestUtils.setField(userResource, "mailService", mockMailService);
        ReflectionTestUtils.setField(userResource, "userMapper", userMapper);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    public void testGetExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/admin")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.lastName").value("Administrator"));
    }

    @Test
    public void testGetUnknownUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void testCreateUser() throws Exception {
        ManagedUserVM userData = new ManagedUserVM();
        AccountResourceIntTest.setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("joe");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Joe");
        userData.setLastName("Shmoe");
        userData.setEmail("joe@example.com");
        userData.setLangKey("en");
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());

        Optional<User> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    @Transactional
    public void testCreateInvalidUser() throws Exception {
        ManagedUserVM userData = new ManagedUserVM();
        AccountResourceIntTest.setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("joe");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Joe");
        userData.setLastName("Shmoe");
        userData.setEmail("joe@example.com");
        userData.setLangKey("en");
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));
        userData.setInstitute(""); // Required

        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isBadRequest());

        Optional<User> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isFalse();
    }

}
