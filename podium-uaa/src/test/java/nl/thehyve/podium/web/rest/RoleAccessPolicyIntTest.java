/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumUaaApp;
import nl.thehyve.podium.common.service.dto.RoleRepresentation;
import nl.thehyve.podium.common.test.Action;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on actions on roles.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = PodiumUaaApp.class)
public class RoleAccessPolicyIntTest extends AbstractUaaAccessPolicyIntTest {

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // Roles

        // GET /roles
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .allow(podiumAdmin, bbmriAdmin));
        // GET /roles/organisation/{uuid}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/organisation/%s", organisationA.getUuid()))
            .allow(podiumAdmin, bbmriAdmin,
                adminOrganisationA, adminOrganisationAandB,
                coordinatorOrganisationA, coordinatorOrganisationAandB,
                reviewerA, reviewerAandB));
        // GET /roles/{id}
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", reviewerBRole.getId()))
            .allow(podiumAdmin, bbmriAdmin));
        // POST /roles. Not allowed!
        actions.add(newAction()
            .setUrl(ROLE_ROUTE).setMethod(HttpMethod.POST)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // DELETE /roles/{id}. Not allowed!
        actions.add(newAction()
            .setUrl(format(ROLE_ROUTE, "/%d", researcherRole.getId()))
            .setMethod(HttpMethod.DELETE)
            .expect(HttpStatus.METHOD_NOT_ALLOWED));
        // PUT /roles (Role role).
        // Edit non-organisation specific role
        RoleRepresentation editedResearcherRole = roleMapper.roleToRoleDTO(researcherRole);
        editedResearcherRole.getUsers().add(bbmriAdmin.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedResearcherRole)
            .allow(podiumAdmin, bbmriAdmin));
        // Edit organisation specific role
        RoleRepresentation editedReviewerARole = roleMapper.roleToRoleDTO(reviewerARole);
        editedReviewerARole.getUsers().add(coordinatorOrganisationA.getUuid());
        actions.add(newAction()
            .setUrl(ROLE_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(editedReviewerARole)
            .allow(podiumAdmin, bbmriAdmin, adminOrganisationA, adminOrganisationAandB));
    }

    @Test
    @Transactional
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
