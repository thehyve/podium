package org.bbmri.podium.web.rest;

import org.bbmri.podium.PodiumUaaApp;
import org.bbmri.podium.domain.User;
import org.bbmri.podium.service.UserService;
import org.bbmri.podium.web.rest.vm.ManagedUserVM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for authentication.
 *
 * See {@link org.bbmri.podium.security.CustomAuthenticationProvider}
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class AuthenticationIntTest {

    Logger log = LoggerFactory.getLogger(AuthenticationIntTest.class);

    @Autowired
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    private static final String testUserName = "test";
    private static final String testEmail = "test@localhost";
    private static final String testPassword = "TestPassword123!";
    private static final String incorrectPassword = "--Incorrect--Password--789--";

    private static final String scope= "web_app:";
    private static final String scopeHeader = "Basic " + Base64.getEncoder().encodeToString(scope.getBytes());

    @Before
    public void setup() {
        ManagedUserVM testUser = new ManagedUserVM();
        testUser.setLogin(testUserName);
        testUser.setPassword(testPassword);
        testUser.setEmail(testEmail);
        User user = userService.registerUser(testUser);
        user.setEmailVerified(true);
        user.setAdminVerified(true);
        userService.save(user);
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    @Transactional
    public void testSuccessfulAuthentication() throws Exception {
        mockMvc.perform(
            post("/oauth/token")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", scopeHeader)
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
    public void testAccountBlockedAfterFailedAttempts() throws Exception {
        // 4 times "Bad credentials"
        for(int i = 0; i < 4; i++) {
            log.info("Attempt {}", i);
            mockMvc.perform(
                post("/oauth/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", scopeHeader)
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
                .header("Authorization", scopeHeader)
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", incorrectPassword)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath("$.error_description").value("The user account is blocked."));
        // Also blocked with correct credentials
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", scopeHeader)
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath("$.error_description").value("The user account is blocked."));
    }

    @Test
    @Transactional
    public void testAccountUnblockedAfterTimeout() throws Exception {
        // 4 times "Bad credentials"
        for(int i = 0; i < 4; i++) {
            log.info("Attempt {}", i);
            mockMvc.perform(
                post("/oauth/token")
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", scopeHeader)
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
                .header("Authorization", scopeHeader)
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", incorrectPassword)
        )
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
        .andExpect(jsonPath("$.error_description").value("The user account is blocked."));
        // Sleep for 4 seconds
        Thread.sleep(4 * 1000);
        // Login successful
        mockMvc.perform(
            post("/oauth/token")
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", scopeHeader)
                .param("grant_type", "password")
                .param("username", testUserName)
                .param("password", testPassword)
        )
        .andExpect(status().isOk());
    }

}
