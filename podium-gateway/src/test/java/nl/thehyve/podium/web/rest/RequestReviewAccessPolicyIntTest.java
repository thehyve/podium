/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.common.test.Action;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static nl.thehyve.podium.common.test.Action.format;
import static nl.thehyve.podium.common.test.Action.newAction;

/**
 * Integration test for the access policy on request review actions.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestReviewAccessPolicyIntTest extends AbstractGatewayAccessPolicyIntTest {

    private Map<UUID, RequestRepresentation> validationRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> approveRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> rejectRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> requestRevisionRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> validatedRejectRequests = new HashMap<>();
    private Map<UUID, RequestRepresentation> validatedRequestRevisionRequests = new HashMap<>();
    private RequestRepresentation reviewRequest;

    Collection<RequestRepresentation> createRequests() throws Exception {
        List<RequestRepresentation> requests = new ArrayList<>();
        reviewRequest = createSubmittedRequest();
        reviewRequest = validateRequest(reviewRequest, coordinatorOrganisationA);
        reviewRequest = fetchRequest(coordinatorOrganisationA, "/" + reviewRequest.getUuid().toString());
        requests.add(reviewRequest);
        for(AuthenticatedUser user: allUsers) {
            UUID userUuid = user == null ? null : user.getUserUuid();
            RequestRepresentation validationRequest = createSubmittedRequest();
            validationRequests.put(userUuid, validationRequest);
            requests.add(validationRequest);

            RequestRepresentation approveRequest = createValidatedRequest();
            approveRequests.put(userUuid, approveRequest);
            requests.add(approveRequest);

            RequestRepresentation rejectRequest = createSubmittedRequest();
            rejectRequests.put(userUuid, rejectRequest);
            requests.add(rejectRequest);

            RequestRepresentation requestRevisionRequest = createSubmittedRequest();
            requestRevisionRequests.put(userUuid, requestRevisionRequest);
            requests.add(requestRevisionRequest);

            RequestRepresentation validatedRejectRequest = createValidatedRequest();
            validatedRejectRequests.put(userUuid, validatedRejectRequest);
            requests.add(validatedRejectRequest);

            RequestRepresentation validatedRequestRevisionRequest = createValidatedRequest();
            validatedRequestRevisionRequests.put(userUuid, validatedRequestRevisionRequest);
            requests.add(validatedRequestRevisionRequest);
        }
        return requests;
    }

    ReviewFeedbackRepresentation getReviewFeedbackForUser(UUID reviewerUuid) {
        ReviewFeedbackRepresentation reviewFeedback = reviewRequest.getReviewRound().getReviewFeedback().stream()
            .filter(feedback -> feedback.getReviewer().getUuid().equals(reviewerUuid))
            .findAny().get();
        reviewFeedback.setAdvice(ReviewProcessOutcome.Approved);
        MessageRepresentation message = new MessageRepresentation();
        message.setSummary("Great request, I like it!");
        message.setDescription("Detailed feedback based on carefully reading the request");
        reviewFeedback.setMessage(message);
        return reviewFeedback;
    }

    private List<Action> actions = new ArrayList<>();

    private void createActions() {
        // GET /requests/{uuid}/validate
        actions.add(newAction()
            .setUrls(getUrlsForUsers(validationRequests, "/%s/validate"))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // GET /requests/{uuid}/approve
        actions.add(newAction()
            .setUrls(getUrlsForUsers(approveRequests, "/%s/approve"))
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // POST /requests/{uuid}/reject
        actions.add(newAction()
            .setUrls(getUrlsForUsers(rejectRequests,"/%s/reject"))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        actions.add(newAction()
            .setUrls(getUrlsForUsers(validatedRejectRequests,"/%s/reject"))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // POST /requests/{uuid}/requestRevision
        actions.add(newAction()
            .setUrls(getUrlsForUsers(requestRevisionRequests, "/%s/requestRevision"))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        actions.add(newAction()
            .setUrls(getUrlsForUsers(validatedRequestRevisionRequests, "/%s/requestRevision"))
            .setMethod(HttpMethod.POST)
            .body(new MessageRepresentation())
            .allow(coordinatorOrganisationA, coordinatorOrganisationAandB));
        // PUT /requests/{uuid}review
        // Test for reviewerAandB
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/review", reviewRequest.getUuid()))
            .setMethod(HttpMethod.PUT)
            .body(getReviewFeedbackForUser(reviewerAandB.getUuid()))
            .allow(reviewerAandB));
        // Test for reviewerA
        actions.add(newAction()
            .setUrl(format(REQUEST_ROUTE, "/%s/review", reviewRequest.getUuid()))
            .setMethod(HttpMethod.PUT)
            .body(getReviewFeedbackForUser(reviewerA.getUuid()))
            .allow(reviewerA));
    }

    @Test
    public void testAccessPolicy() throws Exception {
        setupData();
        createActions();
        runAll(actions, allUsers);
    }

}
