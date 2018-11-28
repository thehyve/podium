/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on request actions.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestRevisionAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    private RequestRepresentation revisionUpdateRequest;
    private Map<UUID, RequestRepresentation> revisionSubmitRequests = new HashMap<>();

    Collection<RequestRepresentation> createRequests() throws Exception {
        List<RequestRepresentation> requests = new ArrayList<>();
        revisionUpdateRequest = createValidatedRequest();
        revisionUpdateRequest = requestRevision(revisionUpdateRequest, coordinatorOrganisationA, new MessageRepresentation());
        revisionUpdateRequest = fetchRequest(researcher, "/" + revisionUpdateRequest.getUuid().toString());
        requests.add(revisionUpdateRequest);
        for(AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();
            RequestRepresentation revisionSubmitRequest = createValidatedRequest();
            revisionSubmitRequest = requestRevision(revisionSubmitRequest, coordinatorOrganisationA, new MessageRepresentation());
            revisionSubmitRequests.put(userUuid, revisionSubmitRequest);
            requests.add(revisionSubmitRequest);
        }
        return requests;
    }

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // PUT /api/requests
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE)
            .setMethod(HttpMethod.PUT)
            .body(revisionUpdateRequest)
            .allow(researcher));
        // GET /api/requests/{uuid}/submit
        actions.add(newAction()
            .setUrls(getUrlsForUsers(revisionSubmitRequests, "/%s/submit"))
            .allow(researcher));
    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
