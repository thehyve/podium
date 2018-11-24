/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on request actions.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    private RequestRepresentation draftRequest1;

    Collection<RequestRepresentation> createRequests() throws Exception {
        draftRequest1 = newDraft(researcher);
        return Arrays.asList(draftRequest1);
    }


    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // GET /requests/drafts
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts")
            .allow(researcher, testUser1, testUser2));
        // POST /requests/drafts
        RequestRepresentation draft = new RequestRepresentation();
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts")
            .setMethod(HttpMethod.POST)
            .body(draft)
            .successStatus(HttpStatus.CREATED)
            .allow(researcher, testUser1, testUser2));
        // GET /requests/drafts/{uuid}
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/drafts/%s", draftRequest1.getUuid()))
            .allow(researcher));
        // PUT /requests/drafts
        // POST /requests/drafts/validate
        // GET /requests/drafts/{uuid}/submit
        // GET /requests/requester
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/requester")
            .allow(researcher, testUser1, testUser2));
        // GET /requests/status/{status}/requester
        // PUT /requests
        // GET /requests/{uuid}/submit
        // GET /requests/reviewer
        // GET /requests/status/{status}/coordinator
        // GET /requests/organisation/{uuid}/reviewer
        // GET /requests/status/{status}/organisation/{uuid}/coordinator
        // GET /requests/{uuid}
        // DELETE /requests/drafts/{uuid}
        // GET /requests/{uuid}/validate
        // GET /requests/{uuid}/reject
        // GET /requests/{uuid}/approve
        // GET /requests/{uuid}/requestRevision
        // GET /_search/requests

    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
