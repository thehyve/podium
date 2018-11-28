/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.PrincipalInvestigatorRepresentation;
import nl.thehyve.podium.common.service.dto.RequestDetailRepresentation;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;
import static nl.thehyve.podium.web.rest.RequestDataHelper.setRequestData;

/**
 * Integration test for the access policy on request actions.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    private RequestRepresentation draftRequest;
    private RequestRepresentation submittedRequest;
    private RequestRepresentation reviewRequest;
    private RequestRepresentation validRequestBody;
    private RequestRepresentation invalidRequestBody = new RequestRepresentation();
    private Map<UUID, RequestRepresentation> submitDraftRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> deleteDraftRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> closeRequests = new HashMap<>();

    Collection<RequestRepresentation> createRequests() throws Exception {
        validRequestBody = new RequestRepresentation();
        validRequestBody.setRequestDetail(new RequestDetailRepresentation());
        validRequestBody.getRequestDetail().setPrincipalInvestigator(new PrincipalInvestigatorRepresentation());
        setRequestData(validRequestBody);

        List<RequestRepresentation> requests = new ArrayList<>();
        draftRequest = newDraft(researcher);
        requests.add(draftRequest);

        submittedRequest = createSubmittedRequest();
        requests.add(submittedRequest);

        reviewRequest = createValidatedRequest();
        requests.add(reviewRequest);

        for(AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();

            RequestRepresentation submitDraftRequest = newDraft(researcher);
            initRequestResourceMock(submitDraftRequest);
            setRequestData(submitDraftRequest);
            submitDraftRequest.getOrganisations().add(organisationA);
            submitDraftRequest = updateDraft(researcher, submitDraftRequest);
            submitDraftRequests.put(userUuid, submitDraftRequest);
            requests.add(submitDraftRequest);

            RequestRepresentation deleteDraftRequest = newDraft(researcher);
            deleteDraftRequests.put(userUuid, deleteDraftRequest);
            requests.add(deleteDraftRequest);

            RequestRepresentation closeRequest = createApprovedRequest();
            closeRequests.put(userUuid, closeRequest);
            requests.add(closeRequest);
        }
        return requests;
    }

    private List<Action> actions = new ArrayList<>();

    private void createDraftActions() {
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
        // PUT /requests/drafts
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts")
            .setMethod(HttpMethod.PUT)
            .body(draftRequest)
            .allow(researcher));
        // POST /requests/drafts/validate
        AuthenticatedUser[] allExceptAnonymous = allUsers.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet())
            .toArray(new AuthenticatedUser[]{});
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts/validate")
            .setMethod(HttpMethod.POST)
            .body(validRequestBody)
            .allow(allExceptAnonymous));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/drafts/validate")
            .setMethod(HttpMethod.POST)
            .body(invalidRequestBody)
            .successStatus(HttpStatus.BAD_REQUEST)
            .allow(allExceptAnonymous));
        // DELETE /requests/drafts/{uuid}
        actions.add(newAction()
            .setUrls(getUrlsForUsers(deleteDraftRequests,"/drafts/%s"))
            .setMethod(HttpMethod.DELETE)
            .allow(researcher));
        // GET /requests/drafts/{uuid}
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/drafts/%s", draftRequest.getUuid()))
            .allow(researcher));
        // GET /requests/drafts/{uuid}/submit
        actions.add(newAction()
            .setUrls(getUrlsForUsers(submitDraftRequests,"/drafts/%s/submit"))
            .allow(researcher));
    }

    private void createOverviewActions() {
        // GET /requests/counts/coordinator
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/counts/coordinator")
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB, coordinatorOrganisationB));
        // GET /requests/counts/requester
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/counts/requester")
            .allow(researcher, testUser1, testUser2));
        // GET /requests/counts/reviewer
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/counts/reviewer")
            .allow(reviewerA, reviewerAandB));
        // GET /requests/organisation/{uuid}/reviewer
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/organisation/%s/reviewer", organisationA.getUuid()))
            .allow(reviewerA, reviewerAandB));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/organisation/%s/reviewer", organisationB.getUuid()))
            .allow(reviewerAandB));
        // GET /requests/requester
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/requester")
            .allow(researcher, testUser1, testUser2));
        // GET /requests/reviewer
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/reviewer")
            .allow(reviewerA, reviewerAandB));
        // GET /requests/status/{status}/coordinator
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/coordinator", OverviewStatus.Draft))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB, coordinatorOrganisationB));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/coordinator", OverviewStatus.Rejected))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB, coordinatorOrganisationB));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/coordinator", OverviewStatus.Review))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB, coordinatorOrganisationB));
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/coordinator", OverviewStatus.Validation))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB, coordinatorOrganisationB));
        // GET /requests/status/{status}/organisation/{uuid}/coordinator
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/organisation/%s/coordinator", OverviewStatus.Validation, organisationA.getUuid()))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // GET /requests/status/{status}/requester
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/status/%s/requester", OverviewStatus.Validation))
            .allow(researcher, testUser1, testUser2));
        // TODO: GET /_search/requests
    }

    private void createRequestActions() {
        // GET /api/requests/{uuid}
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + draftRequest.getUuid().toString())
            .allow(researcher));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + submittedRequest.getUuid().toString())
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB));
        actions.add(newAction()
            .setUrl(REQUEST_ROUTE + "/" + reviewRequest.getUuid().toString())
            .allow(researcher, coordinatorOrganisationA, coordinatorOrganisationAandB, reviewerA, reviewerAandB));
        // POST /api/requests/{uuid}/close
        actions.add(newAction()
            .setUrls(getUrlsForUsers(closeRequests,"/%s/close"))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createDraftActions();
        createOverviewActions();
        createRequestActions();
        runAll(actions, allUsers);
    }

}
