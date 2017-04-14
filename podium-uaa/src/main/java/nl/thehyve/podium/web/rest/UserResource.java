/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.common.config.Constants;
import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.repository.search.UserSearchRepository;
import nl.thehyve.podium.service.MailService;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.service.mapper.UserMapper;
import nl.thehyve.podium.web.rest.vm.ManagedUserVM;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.*;

/**
 * REST controller for managing users.
 *
 * <p>This class accesses the User entity, and needs to fetch its collection of authorities.</p>
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * </p>
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>Another option would be to have a specific JPA entity graph to handle this case.</p>
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Autowired
    private UserMapper userMapper;

    /**
     * POST  /users  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     * </p>
     *
     * @param userData the user to create
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     * @throws UserAccountException when the login already exists.
     * @return the ResponseEntity with status
     * 201 (Created) and with body the new user,
     * 400 (Bad Request) if the login or email is already in use
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @PostMapping("/users")
    @Timed
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRepresentation userData) throws URISyntaxException, UserAccountException {
        log.debug("REST request to save User : {}", userData);

        try {
            User newUser = userService.createUser(userData);
            mailService.sendCreationEmail(newUser);
        } catch(EmailAddressAlreadyInUse e) {
            Optional<User> userOptional = userService.getUserWithAuthoritiesByEmail(userData.getEmail());
            userOptional.ifPresent(user -> mailService.sendAccountAlreadyExists(user));
        } catch (LoginAlreadyInUse e) {
            log.error("Login already in use: {}", userData.getLogin());
            throw e;
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * PUT  /users : Updates an existing User.
     *
     * @param userData the user to update
     * @throws UserAccountException when the login already exists.
     * @return the ResponseEntity with status 200 (OK) and with body the updated user,
     * or with status 400 (Bad Request) if the login or email is already in use,
     * or with status 500 (Internal Server Error) if the user couldn't be updated
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @PutMapping("/users")
    @Timed
    public ResponseEntity<ManagedUserVM> updateUser(@Valid @RequestBody UserRepresentation userData) throws UserAccountException {
        log.debug("REST request to update User : {}", userData);
        userService.updateUser(userData);
        Optional<User> userOptional =  userService.getUserByUuid(userData.getUuid());
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userMapper.userToManagedUserVM(userOptional.get()));
        }
        throw new ResourceNotFound("User not found.");
    }

    /**
     * PUT  /users/uuid/:uuid/unlock : Unlocks an existing User account.
     *
     * @param uuid the uuid of the user to unlock
     * @return the ResponseEntity with status 200 (OK) and with body the updated user,
     * or with status 404 (Not found) if the user could not be found.
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @PutMapping("/users/uuid/{uuid}/unlock")
    @Timed
    public ResponseEntity<ManagedUserVM> unlockUser(@PathVariable UUID uuid) {
        log.debug("REST request to unlock User : {}", uuid);
        Optional<User> userOptional = userService.getUserByUuid(uuid);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found.");
        }
        User user = userService.unlockAccount(userOptional.get());

        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("userManagement.unlocked", user.getLogin()))
            .body(userMapper.userToManagedUserVM(userOptional.get()));
    }

    /**
     * GET  /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     * @throws URISyntaxException if the pagination headers couldn't be generated
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN, AuthorityConstants.ORGANISATION_ADMIN})
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<ManagedUserVM>> getAllUsers(@ApiParam Pageable pageable)
        throws URISyntaxException {
        Page<User> page = userService.getUsers(pageable);
        List<ManagedUserVM> managedUserVMs = userMapper.usersToManagedUserVMs(page.getContent());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(managedUserVMs, headers, HttpStatus.OK);
    }

    /**
     * GET  /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login" user, or with status 404 (Not Found)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<ManagedUserVM> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin(login);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userMapper.userToManagedUserVM(userOptional.get()));
        }
        throw new ResourceNotFound("User not found.");
    }

    /**
     * GET  /users/uuid/:uuid : get the "uuid" user.
     *
     * @param uuid the uuid of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "uuid" user, or with status 404 (Not Found)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN, AuthorityConstants.ORGANISATION_ADMIN})
    @GetMapping("/users/uuid/{uuid}")
    @Timed
    public ResponseEntity<ManagedUserVM> getUserByUuid(@PathVariable UUID uuid) {
        log.debug("REST request to get User : {}", uuid);
        Optional<User> userOptional = userService.getUserByUuid(uuid);
        if (userOptional.isPresent()) {
            return ResponseEntity.ok(userMapper.userToManagedUserVM(userOptional.get()));
        }
        throw new ResourceNotFound("User not found.");
    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        Optional<User> userOptional = userService.getUserWithAuthoritiesByLogin(login);
        if (!userOptional.isPresent()) {
            throw new ResourceNotFound("User not found.");
        }
        userService.delete(userOptional.get());
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build();
    }

    /**
     * SEARCH  /_search/users/:query : search for the User corresponding
     * to the query.
     *
     * @param query the query to search
     * @return the result of the search
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
    @GetMapping("/_search/users")
    @Timed
    public ResponseEntity<List<SearchUser>> search(@RequestParam String query) {
        List<SearchUser> list = userService.search(query);
        return ResponseEntity.ok(list);
    }

    /**
     * SUGGEST  /_suggest/users/:query : Get user suggestions for string
     *
     * @param query the query to search
     * @return the result of the search
     */
    @SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN, AuthorityConstants.ORGANISATION_ADMIN})
    @GetMapping("/_suggest/users")
    @Timed
    public ResponseEntity<List<SearchUser>> suggest(@RequestParam String query) {
        List<SearchUser> list = userService.suggestUsers(query);
        return ResponseEntity.ok(list);
    }
}
