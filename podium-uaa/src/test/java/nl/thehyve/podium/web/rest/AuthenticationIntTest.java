/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.test.OAuth2TokenMockUtil;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.security.CustomServerAuthenticationProvider;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for authentication.
 * <p>
 * See {@link CustomServerAuthenticationProvider}
 */
@ActiveProfiles({"test", "h2"})
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AuthenticationIntTest {

    Logger log = LoggerFactory.getLogger(AuthenticationIntTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private OAuth2TokenMockUtil tokenUtil;

    private MockMvc mockMvc;

    private static final String testUserName = "test";
    private static UUID testUserUuid;
    private static final String testEmail = "test@localhost";
    private static final String testPassword = "TestPassword123!";
    private static final String incorrectPassword = "--Incorrect--Password--789--";

    public static RequestPostProcessor client() {
        return httpBasic("web_app", "");
    }

    private RequestPostProcessor bbmriAdminToken() {
        return tokenUtil.oauth2Authentication(
            "bbmri_admin",
            Sets.newSet("some-client"),
            Sets.newSet(AuthorityConstants.BBMRI_ADMIN));
    }

    @Before
    public void setup() throws UserAccountException {
        ManagedUserVM testUser = new ManagedUserVM();
        testUser.setLogin(testUserName);
        testUser.setPassword(testPassword);
        testUser.setEmail(testEmail);
        testUser.setFirstName("testFirstName");
        testUser.setLastName("testLastName");
        User user = userService.registerUser(testUser);
        user.setEmailVerified(true);
        user.setAdminVerified(true);
        user = userService.save(user);
        testUserUuid = user.getUuid();
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Transactional
    public void testAdminUserAction() throws Exception {
        mockMvc.perform(
            get("/api/users")
                .with(bbmriAdminToken())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testTestUserAuthentication() throws Exception {
        // Create test user
        ManagedUserVM userData = new ManagedUserVM();
        AccountResourceIntTest.setMandatoryFields(userData);
        userData.setId(null);
        userData.setLogin("joe");
        userData.setPassword(AccountResourceIntTest.VALID_PASSWORD);
        userData.setFirstName("Joe");
        userData.setLastName("Shmoe");
        userData.setEmail("joe@example.com");
        userData.setLangKey("en");
        userData.setAdminVerified(true);
        userData.setEmailVerified(true);
        userData.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        mockMvc.perform(post("/api/test/users")
            .with(bbmriAdminToken())
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(userData)))
            .andExpect(status().isCreated());

        // Try to authenticate
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", "joe")
                .param("password", AccountResourceIntTest.VALID_PASSWORD)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.token_type").value("bearer"))
            .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @Transactional
    public void testSuccessfulAuthentication() throws Exception {
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.token_type").value("bearer"))
            .andExpect(jsonPath("$.access_token").isNotEmpty());
    }

    @Test
    @Transactional
    public void testAccountLockedAfterFailedAttempts() throws Exception {
        // 4 times "Bad credentials"
        for (int i = 0; i < 4; i++) {
            log.info("Attempt {}", i);
            mockMvc.perform(
                post("/oauth/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(client())
                    .param("grant_type", "password")
                    .param("username", testUserName)
                    .param("password", incorrectPassword)
            )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.error_description").value("Bad credentials"));
        }
        // 5th time "The user account is blocked."
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", incorrectPassword)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.error_description").value("The user account is locked."));
        // Also blocked with correct credentials
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.error_description").value("The user account is locked."));
    }

    @Test
    @Transactional
    public void testAccountStillLockedAfterTimeout() throws Exception {
        // 4 times "Bad credentials"
        for (int i = 0; i < 4; i++) {
            log.info("Attempt {}", i);
            mockMvc.perform(
                post("/oauth/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(client())
                    .param("grant_type", "password")
                    .param("username", testUserName)
                    .param("password", incorrectPassword)
            )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.error_description").value("Bad credentials"));
        }
        // 5th time "The user account is locked."
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", incorrectPassword)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.error_description").value("The user account is locked."));
        // Sleep for 4 seconds
        Thread.sleep(4 * 1000);
        // Login successful
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.error_description").value("The user account is locked."));
    }

    @Test
    @Transactional
    public void testAccountAvailableAfterUnlock() throws Exception {
        // 4 times "Bad credentials"
        for (int i = 0; i < 4; i++) {
            log.info("Attempt {}", i);
            mockMvc.perform(
                post("/oauth/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .with(client())
                    .param("grant_type", "password")
                    .param("username", testUserName)
                    .param("password", incorrectPassword)
            )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.error_description").value("Bad credentials"));
        }
        // 5th time "The user account is locked."
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", incorrectPassword)
        )
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.error_description").value("The user account is locked."));
        // Unlock account
        mockMvc.perform(
            put("/api/users/uuid/" + testUserUuid.toString() + "/unlock")
                .with(bbmriAdminToken())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk());
        // Login successful
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .with(client())
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
            .andExpect(status().isOk());
    }
}
