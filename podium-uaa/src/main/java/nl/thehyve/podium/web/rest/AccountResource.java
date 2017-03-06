/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;

import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.Public;
import nl.thehyve.podium.domain.User;

import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.security.SecurityService;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.representation.UserRepresentation;
import nl.thehyve.podium.validation.PasswordValidator;
import nl.thehyve.podium.web.rest.vm.KeyAndPasswordVM;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import nl.thehyve.podium.web.rest.util.HeaderUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Inject
    private UserService userService;

    @Inject
    private MailService mailService;

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or e-mail is already in use
     */
    @Public
    @PostMapping(path = "/register",
                    produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    @Timed
    public ResponseEntity<?> registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) throws UserAccountException {
        HttpHeaders textPlainHeaders = new HttpHeaders();
        textPlainHeaders.setContentType(MediaType.TEXT_PLAIN);
        try {
            User user = userService.registerUser(managedUserVM);
            mailService.sendVerificationEmail(user);
        } catch(EmailAddressAlreadyInUse e) {
            Optional<User> userOptional = userService.getUserWithAuthoritiesByEmail(managedUserVM.getEmail());
            if (userOptional.isPresent()) {
                mailService.sendAccountAlreadyExists(userOptional.get());
            }
        } catch (LoginAlreadyInUse e) {
            log.error("Login already in use: {}", managedUserVM.getLogin());
            throw e;
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return  the ResponseEntity with status 200 (OK) and the activated user in body,
     *          or status 500 (Internal Server Error) if the user couldn't be activated
     */
    @Public
    @GetMapping("/verify")
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        try {
            Optional<User> user = userService.verifyRegistration(key);

            if (user.isPresent() && user.get().getActivationKey() == null) {
                return new ResponseEntity<String>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(VerificationKeyExpired vke) {
            return new ResponseEntity<>("renew", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Public
    @GetMapping("/reverify")
    @Timed
    public ResponseEntity<String> renewVerification(@RequestParam(value = "key") String key) {
        return userService.renewVerificationKey(key)
            .map(user -> new ResponseEntity<String>(HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the ResponseEntity with status 200 (OK) and the current user in body,
     * or status 500 (Internal Server Error) if the user couldn't be returned
     */
    @AnyAuthorisedUser
    @GetMapping("/account")
    @Timed
    public ResponseEntity<UserRepresentation> getAccount() {
        return Optional.ofNullable(userService.getUserWithAuthorities())
            .map(user -> new ResponseEntity<>(new UserRepresentation(user), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     */
    @AnyAuthorisedUser
    @PostMapping("/account")
    @Timed
    public ResponseEntity<String> saveAccount(@Valid @RequestBody UserRepresentation userDTO) {
        Optional<User> existingUser = userService.getUserWithAuthoritiesByEmail(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userDTO.getLogin()))) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("user-management", "emailexists", "Email already in use")).body(null);
        }
        return userService
            .getUserWithAuthoritiesByLogin(SecurityService.getCurrentUserLogin())
            .map(u -> {
                userService.updateUserAccount(userDTO);
                return new ResponseEntity<String>(HttpStatus.OK);
            })
            .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * POST  /account/change_password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
     */
    @AnyAuthorisedUser
    @PostMapping(path = "/account/change_password",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> changePassword(@RequestBody String password) {
        if (!PasswordValidator.validate(password)) {
            return new ResponseEntity<>("Incorrect password", HttpStatus.BAD_REQUEST);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * POST   /account/reset_password/init : Send an e-mail to reset the password of the user
     *
     * @param mail the mail of the user
     * @return the ResponseEntity with status 200 (OK) if the e-mail was sent, or status 400 (Bad Request) if the e-mail address is not registered
     */
    @Public
    @PostMapping(path = "/account/reset_password/init",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<?> requestPasswordReset(@RequestBody String mail) {
        return userService.requestPasswordReset(mail)
            .map(user -> {
                mailService.sendPasswordResetMail(user);
                return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
            }).orElseGet(() -> {
                mailService.sendPasswordResetMailNoUser(mail);
                return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
            });
    }

    /**
     * POST   /account/reset_password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @return the ResponseEntity with status 200 (OK) if the password has been reset,
     * or status 400 (Bad Request) or 500 (Internal Server Error) if the password could not be reset
     */
    @Public
    @PostMapping(path = "/account/reset_password/finish",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        return userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
              .map(user -> new ResponseEntity<String>(HttpStatus.OK))
              .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
