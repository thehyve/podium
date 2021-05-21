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
import nl.thehyve.podium.common.test.AbstractAuthorisedUserIntTest;
import nl.thehyve.podium.common.test.web.rest.TestUtil;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.AuthorityRepository;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.web.rest.dto.KeyAndPasswordRepresentation;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the AccountResource REST controller.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PodiumUaaApp.class)
public class AccountResourceIntTest extends AbstractAuthorisedUserIntTest {

    static final String VALID_PASSWORD = "johndoe2!";

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private TestService testService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebApplicationContext context;

    @Mock
    private UserService mockUserService;

    @Mock
    private MailService mockMailService;

    private MockMvc restUserMockMvc;

    private MockMvc restMvc;

    private MockMvc mockMvc;

    @Override
    protected MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mockMailService).sendAccountAlreadyExists(anyObject());
        doNothing().when(mockMailService).sendVerificationEmail(any(UserRepresentation.class), anyString());
        doNothing().when(mockMailService).sendPasswordResetMail(any(UserRepresentation.class), anyString());
        doNothing().when(mockMailService).sendUserRegisteredEmail(
            anyCollectionOf(ManagedUserRepresentation.class), any(ManagedUserRepresentation.class));

        ReflectionTestUtils.setField(userService, "mailService", mockMailService);

        AccountResource accountResource = new AccountResource();
        ReflectionTestUtils.setField(accountResource, "userService", userService);

        AccountResource accountUserMockResource = new AccountResource();
        ReflectionTestUtils.setField(accountUserMockResource, "userService", mockUserService);

        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).build();
        this.restUserMockMvc = MockMvcBuilders.standaloneSetup(accountUserMockResource).build();
        this.mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<String> authorities = new HashSet<>();
        authorities.add(AuthorityConstants.PODIUM_ADMIN);

        ManagedUserRepresentation user = new ManagedUserRepresentation();
        user.setLogin("test");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@bbmri-podium.com");
        user.setAuthorities(authorities);
        when(mockUserService.getUserWithAuthorities()).thenReturn(user);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.login").value("test"))
                .andExpect(jsonPath("$.firstName").value("john"))
                .andExpect(jsonPath("$.lastName").value("doe"))
                .andExpect(jsonPath("$.email").value("john.doe@bbmri-podium.com"))
                .andExpect(jsonPath("$.authorities").value(AuthorityConstants.PODIUM_ADMIN));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        when(mockUserService.getUserWithAuthorities()).thenReturn(null);

        restUserMockMvc.perform(get("/api/account")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    /**
     * Set all mandatory fields except first name, last name, username, email.
     * @param user the object to set the fields on.
     */
    static void setMandatoryFields(UserRepresentation user) {
        user.setJobTitle("Tester");
        user.setInstitute("Software institute");
        user.setDepartment("Testing");
        user.setTelephone("123456789");
        user.setSpecialism("Microservice architectures");
    }

    @Test
    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("joe");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Joe");
        validUser.setLastName("Shmoe");
        validUser.setEmail("joe@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("joe");
        assertThat(user.isPresent()).isTrue();

        Thread.sleep(1000);

        verify(mockMailService).sendVerificationEmail(any(ManagedUserRepresentation.class), anyString());
    }

    @Test
    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserRepresentation invalidUser = new ManagedUserRepresentation();
        setMandatoryFields(invalidUser);
        invalidUser.setId(null);
        invalidUser.setLogin("funky-log!n"); // invalid
        invalidUser.setPassword(VALID_PASSWORD);
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserRepresentation invalidUser = new ManagedUserRepresentation();
        setMandatoryFields(invalidUser);
        invalidUser.setId(null);
        invalidUser.setLogin("bob");
        invalidUser.setPassword(VALID_PASSWORD);
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("invalid"); // invalid
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("bob");
        assertThat(user.isPresent()).isFalse();
    }

    StringBuilder tooLongPassword = new StringBuilder();
    {
        for (int i = 0; i < 100; i++) {
            tooLongPassword.append("Abcdef12345%^&*");
        }
    }
    String[] invalidPasswords = {
        null, // empty password
        "", // empty password
        "1234567", // password with less than 8 characters
        "12345678", // password with only numbers
        "abcde123", // password without special characters
        "abc&%$;.Y", // password without numbers
        "123456^&*(", // password without alphabetical symbols
        tooLongPassword.toString() // password larger than 1000 characters
    };

    @Test
    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        for(String password: invalidPasswords) {
            ManagedUserRepresentation invalidUser = new ManagedUserRepresentation();
            setMandatoryFields(invalidUser);
            invalidUser.setId(null);
            invalidUser.setLogin("bob");
            invalidUser.setPassword(password);
            invalidUser.setFirstName("Bob");
            invalidUser.setLastName("Green");
            invalidUser.setEmail("bob@example.com");
            invalidUser.setLangKey("en");
            invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

            restUserMockMvc.perform(
                post("/api/register")
                    .contentType(TestUtil.APPLICATION_JSON_UTF8)
                    .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
                .andExpect(status().isBadRequest());

            Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByLogin("bob");
            assertThat(user.isPresent()).isFalse();
        }
    }

    private ManagedUserRepresentation duplicateManagedUserVM(ManagedUserRepresentation original) {
        ManagedUserRepresentation duplicate = new ManagedUserRepresentation();
        duplicate.setId(original.getId());
        duplicate.setLogin(original.getLogin());
        duplicate.setPassword(original.getPassword());
        duplicate.setFirstName(original.getFirstName());
        duplicate.setLastName(original.getLastName());
        duplicate.setEmail(original.getEmail());
        duplicate.setLangKey(original.getLangKey());
        duplicate.setAuthorities(original.getAuthorities());
        duplicate.setJobTitle(original.getJobTitle());
        duplicate.setInstitute(original.getInstitute());
        duplicate.setDepartment(original.getDepartment());
        duplicate.setTelephone(original.getTelephone());
        duplicate.setSpecialism(original.getSpecialism());
        return duplicate;
    }

    @Test
    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // Good
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("alice");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Alice");
        validUser.setLastName("Something");
        validUser.setEmail("alice@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        // Duplicate login, different e-mail
        ManagedUserRepresentation duplicatedUser = duplicateManagedUserVM(validUser);
        duplicatedUser.setEmail("alicejr@example.com");

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate login
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().is4xxClientError());

        Optional<ManagedUserRepresentation> userDup = userService.getUserWithAuthoritiesByEmail("alicejr@example.com");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // Good
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("john");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setEmail("john@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        // Duplicate e-mail, different login
        ManagedUserRepresentation duplicatedUser = duplicateManagedUserVM(validUser);
        duplicatedUser.setLogin("johnjr");

        // Good user
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        // Duplicate e-mail
        // Status CREATED is returned, but the account is not created. A notification
        // email is sent instead.
        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(duplicatedUser)))
            .andExpect(status().isCreated());

        verify(mockMailService).sendAccountAlreadyExists(anyObject());

        Optional<ManagedUserRepresentation> userDup = userService.getUserWithAuthoritiesByLogin("johnjr");
        assertThat(userDup.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<ManagedUserRepresentation> userDup = userService.getUserWithAuthoritiesByLogin("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getAuthorities()).hasSize(1)
            .containsExactly(authorityRepository.findById(AuthorityConstants.RESEARCHER).get().getName());
    }

    @Test
    @Transactional
    public void testVerifyUserEmail() throws Exception {
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Thread.sleep(1000);
        verify(mockMailService).sendVerificationEmail(any(ManagedUserRepresentation.class), anyString());
        reset(mockMailService);

        userService.getDomainUserWithAuthoritiesByLogin("badguy")
            .map(user -> {
                assertThat(user.getActivationKey() != null);

                try {
                    restMvc.perform(get("/api/verify")
                        .param("key", user.getActivationKey()))
                        .andExpect(status().isOk());
                } catch (Exception ex) { }

                return user;
            });

        Thread.sleep(1000);
        verify(mockMailService).sendUserRegisteredEmail(
            anyCollection(), any(UserRepresentation.class));
    }

    @Test
    @Transactional
    public void testVerifyUserByResetLink() throws Exception {
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Thread.sleep(1000);
        verify(mockMailService).sendVerificationEmail(any(ManagedUserRepresentation.class), anyString());
        reset(mockMailService);

        restMvc.perform(
            post("/api/account/reset_password/init")
                .contentType(MediaType.TEXT_PLAIN)
                .content("badguy@example.com"))
            .andExpect(status().isOk());

        Thread.sleep(1000);
        verify(mockMailService).sendPasswordResetMail(any(ManagedUserRepresentation.class), anyString());
        reset(mockMailService);

        userService.getDomainUserWithAuthoritiesByLogin("badguy")
            .map(user -> {
                assertThat(user.getResetKey() != null);

                try {
                    KeyAndPasswordRepresentation keyAndPasswordRepresentation = new KeyAndPasswordRepresentation();
                    keyAndPasswordRepresentation.setKey(user.getResetKey());
                    keyAndPasswordRepresentation.setNewPassword(VALID_PASSWORD);
                    restMvc.perform(
                        post("/api/account/reset_password/finish")
                            .contentType(TestUtil.APPLICATION_JSON_UTF8)
                            .content(TestUtil.convertObjectToJsonBytes(keyAndPasswordRepresentation)))
                        .andExpect(status().isOk());
                } catch (Exception ex) { }

                return user;
            });

        Thread.sleep(1000);
        verify(mockMailService).sendUserRegisteredEmail(
            anyCollection(), any(UserRepresentation.class));
    }

    @Test
    @Transactional
    public void testVerifyUserEmailInvalid() throws Exception {
        ManagedUserRepresentation validUser = new ManagedUserRepresentation();
        setMandatoryFields(validUser);
        validUser.setId(null);
        validUser.setLogin("badguy");
        validUser.setPassword(VALID_PASSWORD);
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setLangKey("en");
        validUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.PODIUM_ADMIN)));

        restMvc.perform(
            post("/api/register")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        userService.getDomainUserWithAuthoritiesByLogin("badguy")
            .map(user -> {
                assertThat(user.getActivationKey() != null);

                try {
                    Thread.sleep(4400);

                    MvcResult result = restMvc.perform(get("/api/verify")
                        .param("key", user.getActivationKey()))
                        .andExpect(status().is5xxServerError())
                        .andReturn();

                    assertThat(result.getResponse().getContentAsString()).isEqualTo("renew");
                } catch (Exception ex) {}

                return user;
            });
    }

    @Test
    @Transactional
    public void testSaveInvalidLogin() throws Exception {
        UserRepresentation invalidUser = new UserRepresentation();
        setMandatoryFields(invalidUser);
        invalidUser.setLogin("funky-log!n");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));

        restUserMockMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByEmail("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    /**
     * Test that changes to email, login and authorities of own account are not saved.
     */
    @Test
    @Transactional
    public void testSensitiveFieldsNotSaved() throws Exception {
        User protectedUser = testService.createUser("protectedUser", AuthorityConstants.RESEARCHER);
        ManagedUserRepresentation userVM = userMapper.userToManagedUserVM(protectedUser);
        UserRepresentation updatedUserVM = userMapper.userToUserDTO(protectedUser);

        // Change non-sensitive user data
        updatedUserVM.setDepartment("New department");
        updatedUserVM.setInstitute("Test institute");
        updatedUserVM.setSpecialism("Testing");
        updatedUserVM.setJobTitle("Tester");
        updatedUserVM.setTelephone("0123456789");

        // Change sensitive user data
        updatedUserVM.setLogin("protectedUser_changed");
        updatedUserVM.setEmail("protectedUser_changed@localhost");
        updatedUserVM.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.BBMRI_ADMIN)));
        updatedUserVM.setEmailVerified(true);

        mockMvc.perform(post("/api/account")
            .with(token(protectedUser))
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedUserVM)))
            .andExpect(status().isOk());

        // Check that no user 'protectedUser_changed' exists
        Optional<User> userWithChangedLogin = userService.getDomainUserWithAuthoritiesByLogin("protectedUser_changed");
        assertThat(userWithChangedLogin.isPresent()).isFalse();

        Optional<User> updatedUser = userService.getDomainUserWithAuthoritiesByLogin(userVM.getLogin());
        assertThat(updatedUser.isPresent()).isTrue();
        updatedUser.map(user -> {
            // Check that some fields have been changed
            assertThat(user.getDepartment()).isEqualTo(updatedUserVM.getDepartment());

            // Check that sensitive fields have not been changed.
            assertThat(user.getEmail()).isEqualTo(userVM.getEmail());
            assertThat(user.getAuthorityNames()).isEqualTo(userVM.getAuthorities());
            assertThat(user.isEmailVerified()).isFalse();
            return user;
        });
    }

    /**
     * Test that changes to email, login and authorities of own account are not saved.
     */
    @Test
    @Transactional
    public void testPasswordUpdateValidation() throws Exception {
        for (String password: invalidPasswords) {
            if (password != null) {
                restUserMockMvc.perform(post("/api/account/change_password")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(password))
                    .andExpect(status().isBadRequest());
            }
        }
    }

    @Test
    @Transactional
    public void testIncompleteRegistrationForm() throws Exception {
        UserRepresentation invalidUser = new UserRepresentation();
        setMandatoryFields(invalidUser);
        invalidUser.setLogin("badguy");
        invalidUser.setFirstName("Bad");
        invalidUser.setLastName("Guy");
        invalidUser.setEmail("badguy@example.com");
        invalidUser.setLangKey("en");
        invalidUser.setAuthorities(new HashSet<>(Arrays.asList(AuthorityConstants.RESEARCHER)));
        invalidUser.setDepartment(""); // required field

        restUserMockMvc.perform(
            post("/api/account")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<ManagedUserRepresentation> user = userService.getUserWithAuthoritiesByEmail("badguy@example.com");
        assertThat(user.isPresent()).isFalse();
    }

}
