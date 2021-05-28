/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.security.annotations.AnyAuthorisedUser;
import nl.thehyve.podium.common.security.annotations.Public;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.exceptions.VerificationKeyExpired;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.validation.PasswordValidator;
import nl.thehyve.podium.web.rest.dto.KeyAndPasswordRepresentation;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    @Autowired
    private UserService userService;

    /**
     * POST  /register : register the user.
     *
     * @param managedUserRepresentation the managed user View Model
     * @throws UserAccountException Exception thrown when a user login is already in use.
     * @return the ResponseEntity with status 201 (Created) if the user is registered or 400 (Bad Request) if the login or e-mail is already in use
     */
    @Public
    @PostMapping(path = "/register",
                    produces={MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> registerAccount(@Valid @RequestBody ManagedUserRepresentation managedUserRepresentation) throws UserAccountException {
        userService.registerUser(managedUserRepresentation);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @return  the ResponseEntity with status
     *          200 (OK)
     *          500 (Internal Server Error) if the user couldn't be activated
     */
    @Public
    @GetMapping("/verify")
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        try {
            if (userService.verifyRegistration(key)) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(VerificationKeyExpired vke) {
            return new ResponseEntity<>("renew", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Public
    @GetMapping("/reverify")
    public ResponseEntity<String> renewVerification(@RequestParam(value = "key") String key) {
        if (userService.renewVerificationKey(key)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * GET  /account : get the current user.
     *
     * @return  the ResponseEntity with status
     *          200 (OK) and the current user in body,
     *          500 (Internal Server Error) if the user couldn't be returned
     */
    @AnyAuthorisedUser
    @GetMapping("/account")
    public ResponseEntity<UserRepresentation> getAccount() {
        UserRepresentation user = userService.getUserWithAuthorities();
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(user);
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) or 500 (Internal Server Error) if the user couldn't be updated
     * @throws UserAccountException if login or email already in use.
     */
    @AnyAuthorisedUser
    @PostMapping("/account")
    public UserRepresentation saveAccount(@Valid @RequestBody UserRepresentation userDTO) throws UserAccountException {
        return userService.updateUserAccount(userDTO);
    }

    /**
     * POST  /account/change_password : changes the current user's password
     *
     * @param password the new password
     * @return the ResponseEntity with status 200 (OK), or status 400 (Bad Request) if the new password is not strong enough
     */
    @AnyAuthorisedUser
    @PostMapping(path = "/account/change_password",
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
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
        consumes = MediaType.TEXT_PLAIN_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> requestPasswordReset(@RequestBody String mail) {
        userService.requestPasswordReset(mail);
        return new ResponseEntity<>("e-mail was sent", HttpStatus.OK);
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
    public ResponseEntity<String> finishPasswordReset(@RequestBody KeyAndPasswordRepresentation keyAndPassword) {
        if (userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
