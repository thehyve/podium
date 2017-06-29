/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.domain.Organisation;
import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.exceptions.EmailAddressAlreadyInUse;
import nl.thehyve.podium.exceptions.LoginAlreadyInUse;
import nl.thehyve.podium.exceptions.UserAccountException;
import nl.thehyve.podium.service.OrganisationService;
import nl.thehyve.podium.service.TestService;
import nl.thehyve.podium.service.UserService;
import nl.thehyve.podium.service.dto.TestRoleRepresentation;
import nl.thehyve.podium.web.rest.dto.ManagedUserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/test")
@SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
public class TestResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    @Autowired
    private UserService userService;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private TestService testService;

    /**
     * POST  /test/users  : Creates a new user for testing purposes.
     * <p>
     * Creates a new user if the login and email are not already used.
     * Email verification, admin verification and account locking status can also be set.
     * </p>
     *
     * @param userData the user to create.
     * @return the ResponseEntity with status 201 (Created),
     *      or with status 400 (Bad Request) if the login or email is already in use.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("users")
    @Timed
    public ResponseEntity<?> createUser(@Valid @RequestBody ManagedUserRepresentation userData) throws URISyntaxException, UserAccountException {
        log.debug("REST request to save test User : {}", userData);

        try {
            User user = userService.createUser(userData);
            user.setEmailVerified(userData.isEmailVerified());
            user.setAdminVerified(userData.isAdminVerified());
            user.setAccountLocked(userData.isAccountLocked());
            if (user.isAccountLocked()) {
                user.setAccountLockDate(ZonedDateTime.now());
            }
            user = userService.save(user);
            userService.changePassword(user, userData.getPassword());
        } catch(EmailAddressAlreadyInUse e) {
            log.error("Email already in use: {}", userData.getEmail());
            throw e;
        } catch (LoginAlreadyInUse e) {
            log.error("Login already in use: {}", userData.getLogin());
            throw e;
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * POST  /test/organisations : Create a new organisation for testing purposes.
     * Also sets organisation activation.
     *
     * @param organisationData the organisation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new organisation, or with status 400 (Bad Request) if the organisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("organisations")
    @Timed
    public ResponseEntity<OrganisationRepresentation> createOrganisation(@Valid @RequestBody OrganisationRepresentation organisationData) throws URISyntaxException {
        log.debug("REST request to save test Organisation : {}", organisationData);
        if (organisationData.getId() != null) {
            return ResponseEntity.badRequest().body(null);
        }
        Organisation organisation = new Organisation();
        organisation.setName(organisationData.getName());
        organisation.setShortName(organisationData.getShortName());
        organisation.setActivated(organisationData.getActivated());
        organisation.setRequestTypes(organisationData.getRequestTypes());
        organisation = organisationService.save(organisation);

        OrganisationRepresentation result = new OrganisationRepresentation();
        result.setId(organisation.getId());
        result.setUuid(organisation.getUuid());
        result.setName(organisation.getName());
        result.setShortName(organisation.getShortName());
        result.setActivated(organisation.isActivated());
        result.setRequestTypes(organisation.getRequestTypes());
        return ResponseEntity.created(new URI("/api/organisations/" + organisation.getId()))
            .body(result);
    }

    /**
     * POST /test/roles/assign
     * Assigns users by UUID to a role. The role is identified by the authority name and
     * (if applicable) the organisation UUID.
     *
     * @param roleData the role identifiers and the set of user uuids.
     * @return status code {@link HttpStatus#CREATED}.
     */
    @PostMapping("roles/assign")
    @Timed
    public ResponseEntity<?> assignUsersToRole(@RequestBody TestRoleRepresentation roleData) {
        testService.assignUsersToRole(roleData);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * GET /test/clearDatabase : Clears database except admin and system user accounts.
     */
    @GetMapping("clearDatabase")
    @Timed
    public void clearDatabase() {
        testService.clearDatabase();
    }

}
