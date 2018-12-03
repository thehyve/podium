/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.RequestFileType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URL;
import java.util.*;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on request files.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestFileAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    static final String TEST_FILE = "test/images/KCOV_Excelsior_-_MP_2008.png";

    private RequestRepresentation draftRequest;
    private RequestFileRepresentation draftRequestFile;
    private RequestRepresentation validationRequest;
    private RequestFileRepresentation validationRequestFile;
    private RequestRepresentation reviewRequest;
    private RequestRepresentation approvedRequest;

    private Map<UUID, RequestFileRepresentation> draftDeleteFiles = new HashMap<>();
    private Map<UUID, RequestFileRepresentation> validationDeleteFiles = new HashMap<>();

    Collection<RequestRepresentation> createRequests() throws Exception {
        draftRequest = newDraft(researcher);
        initRequestResourceMock(draftRequest);
        draftRequestFile = uploadRequestFile(researcher, draftRequest, TEST_FILE);
        draftRequestFile.setRequestFileType(RequestFileType.ORG_CONDITIONS);

        validationRequest = createSubmittedRequest();
        validationRequestFile = uploadRequestFile(coordinatorOrganisationA, validationRequest, TEST_FILE);
        validationRequestFile.setRequestFileType(RequestFileType.METC_LETTER);

        reviewRequest = createValidatedRequest();
        approvedRequest = createApprovedRequest();

        for(AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();
            RequestFileRepresentation draftDeleteFile = uploadRequestFile(researcher, draftRequest, TEST_FILE);
            draftDeleteFiles.put(userUuid, draftDeleteFile);
            RequestFileRepresentation validationDeleteFile = uploadRequestFile(coordinatorOrganisationA, validationRequest, TEST_FILE);
            validationDeleteFiles.put(userUuid, validationDeleteFile);
        }
        return Arrays.asList(draftRequest, validationRequest, reviewRequest, approvedRequest);
    }

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // POST /requests/{uuid}/files
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(TEST_FILE);
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + draftRequest.getUuid().toString() + "/files")
            .setMethod(HttpMethod.POST)
            .body(resource)
            .successStatus(HttpStatus.CREATED)
            .allow(researcher));
        // GET /requests/{uuid}/files/{fileuuid}/download
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/files/%s/download", draftRequest.getUuid(), draftRequestFile.getUuid()))
            .allow(researcher));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/files/%s/download", validationRequest.getUuid(), validationRequestFile.getUuid()))
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        // GET /requests/{uuid}/files
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + draftRequest.getUuid().toString() + "/files")
            .allow(researcher));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + validationRequest.getUuid().toString() + "/files")
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + reviewRequest.getUuid().toString() + "/files")
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB, reviewerA, reviewerAandB));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + approvedRequest.getUuid().toString() + "/files")
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        // DELETE /requests/{uuid}/files/{fileuuid}
        actions.add(newAction()
            .setUrls(getUrlsForUsers(draftDeleteFiles, "/" + draftRequest.getUuid().toString() + "/files/%s"))
            .setMethod(HttpMethod.DELETE)
            .allow(researcher));
        actions.add(newAction()
            .setUrls(getUrlsForUsers(validationDeleteFiles, "/" + validationRequest.getUuid().toString() + "/files/%s"))
            .setMethod(HttpMethod.DELETE)
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // PUT /requests/{uuid}/files/{fileuuid}/type
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/files/%s/type", draftRequest.getUuid(), draftRequestFile.getUuid()))
            .setMethod(HttpMethod.PUT)
            .body(draftRequestFile)
            .allow(researcher));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/files/%s/type", validationRequest.getUuid(), validationRequestFile.getUuid()))
            .setMethod(HttpMethod.PUT)
            .body(validationRequestFile)
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
