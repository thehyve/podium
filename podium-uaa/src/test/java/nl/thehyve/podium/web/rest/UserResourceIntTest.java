/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.common.test.web.rest.TestUtil;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static nl.thehyve.podium.web.rest.AccountResourceIntTest.setMandatoryFields;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
@Transactional
public class UserResourceIntTest {

    @Autowired
    private UserService userService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    private MockMvc restAccountMockMvc;


    private static ManagedUserRepresentation createTestUserData() {
        ManagedUserRepresentation userData = new ManagedUserRepresentation();
        setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("joe");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Joe");
        userData.setLastName("Shmoe");
        userData.setEmail("joe@example.com");
        userData.setLangKey("en");
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));
        return userData;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendVerificationEmail(anyObject(), anyString());
        doNothing().when(mockMailService).sendUserRegisteredEmail(
            anyCollectionOf(UserRepresentation.class), any(UserRepresentation.class));
        doNothing().when(mockMailService).sendAccountVerifiedEmail(anyObject());

        ReflectionTestUtils.setField(userService, "mailService", mockMailService);

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userService", userService);
        this.restAccountMockMvc = MockMvcBuilders.standaloneSetup(accountResource).build();

        UserResource userResource = new UserResource();
        ReflectionTestUtils.setField(userResource, "userService", userService);
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(userResource).build();
    }

    @Test
    public void testGetExistingUser() throws Exception {
        ManagedUserRepresentation userData = createTestUserData();
        userService.createUser(userData);

        restUserMockMvc.perform(get("/api/users/joe")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.lastName").value("Shmoe"));
    }

    @Test
    public void testGetUnknownUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateUser() throws Exception {
        ManagedUserRepresentation userData = createTestUserData();

        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isTrue();
    }

    @Test
    public void testCreateInvalidUser() throws Exception {
        ManagedUserRepresentation userData = createTestUserData();
        userData.setInstitute(""); // Required

        restUserMockMvc.perform(post("/api/users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isBadRequest());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    public void testVerifyAccount() throws Exception {
        ManagedUserRepresentation userData = createTestUserData();
        setMandatoryFields(userData);

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isTrue();

        Thread.sleep(1000);

        verify(mockMailService).sendVerificationEmail(anyObject(), anyString());

        userData = user.get();
        userData.setAdminVerified(true);

        restUserMockMvc.perform(
            put("/api/users")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isOk());

        Thread.sleep(1000);

        verify(mockMailService).sendAccountVerifiedEmail(anyObject());
    }

}
