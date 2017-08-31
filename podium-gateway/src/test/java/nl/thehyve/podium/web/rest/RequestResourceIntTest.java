/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.OrganisationRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.ReviewRoundRepository;
import org.apache.commons.collections.map.HashedMap;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestResource REST controller.
 *
 * @see RequestResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestResourceIntTest extends AbstractRequestDataIntTest {

    @Autowired
    private ReviewRoundRepository reviewRoundRepository;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Test
    public void createDraft() throws Exception {
        long databaseSizeBeforeCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();

        RequestRepresentation request = newDraft(requester);

        Assert.assertNotNull(request.getUuid());

        long databaseSizeAfterCreate = requestRepository
                .findAllByRequesterAndStatus(mockRequesterUuid, RequestStatus.Draft, null).getTotalElements();
        Assert.assertEquals(databaseSizeBeforeCreate + 1, databaseSizeAfterCreate);
    }

    @Test
    public void fetchDrafts() throws Exception {
        RequestRepresentation request1 = newDraft(requester);
        RequestRepresentation request2 = newDraft(requester);
        Set<UUID> requestUuids = new TreeSet<>(Arrays.asList(request1.getUuid(), request2.getUuid()));

        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts",
                null,
                Collections.emptyMap())
            .with(token(requester))
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(requestUuids.size(), requests.size());
            Set<UUID> resultUuids = new TreeSet<>();
            for(RequestRepresentation req: requests) {
                resultUuids.add(req.getUuid());
            }
            Assert.assertEquals(requestUuids, resultUuids);
        });
    }

    @Test
    public void deleteDraft() throws Exception {
        RequestRepresentation request = newDraft(requester);

        mockMvc.perform(
            getRequest(HttpMethod.DELETE,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString(),
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });

        Request req = requestRepository.findOneByUuid(request.getUuid());
        Assert.assertNull(req);
    }

    @Test
    public void submitDraft() throws Exception {
        initMocks();
        // Setup a submitted draft
        getSubmittedDraft();

        Thread.sleep(2000);

        verify(this.mailService, times(1)).sendSubmissionNotificationToCoordinators(any(), any(), nonEmptyUserRepresentationList());
        verify(this.mailService, times(1)).sendSubmissionNotificationToRequester(any(), nonEmptyRequestList());
        verify(this.auditService, times(1)).publishEvent(any());

        // Fetch requests with status 'Validation'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
                long historicEventCount = requestRepository.countHistoricEventsByRequestUuid(req.getUuid());
                Assert.assertEquals(1, historicEventCount);
            }
        });
    }

    @Test
    public void requestsContainRelatedRequestData() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                List<RequestRepresentation> requests =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);

                List<RequestRepresentation> twoOrganisationsRequests = requests.stream()
                    .filter(req -> req.getRequestDetail().getTitle().equals(TEST_TWO_ORGANISATIONS_TITLE))
                    .collect(Collectors.toList());

                Assert.assertEquals(2, twoOrganisationsRequests.size());
                // Test if each of the two requests contains a link to the other.
                int i = 0;
                for(RequestRepresentation req: twoOrganisationsRequests) {
                    int j = i == 0 ? 1 : 0;
                    RequestRepresentation other = twoOrganisationsRequests.get(j);
                    Assert.assertThat(other.getOrganisations(), hasSize(1));
                    OrganisationRepresentation otherOrganisation = other.getOrganisations().get(0);
                    Assert.assertThat(req.getRelatedRequests(), hasSize(1));
                    Assert.assertThat(req.getRelatedRequests(), hasItem(
                        allOf(
                            hasProperty("uuid", equalTo(other.getUuid())),
                            hasProperty("requestDetail",
                                hasProperty("requestType", equalTo(other.getRequestDetail().getRequestType()))),
                            hasProperty("organisations", hasItem(
                                allOf(
                                    hasProperty("uuid", equalTo(otherOrganisation.getUuid())),
                                    hasProperty("name", equalTo(otherOrganisation.getName()))
                                )
                            ))
                        )
                    ));
                    i++;
                }
            });
    }

    @Test
    public void fetchRequesterRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation'
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(3, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
            }
        });
    }

    @Test
    public void fetchRequesterRequestCounts() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> requests = fetchAllForRole(requester, AuthorityConstants.RESEARCHER);
        Assert.assertEquals(3, requests.size());

        RequestRepresentation organisation1Request = requests.stream().filter(req ->
            req.getOrganisations().stream().anyMatch(org -> org.getUuid().equals(organisationUuid1))
        ).findAny().get();
        validateRequest(organisation1Request, coordinator1);

        // Fetch request counts
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/counts/requester",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                Map<OverviewStatus, Long> counts =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), countsTypeReference);
                Assert.assertEquals(3, counts.get(OverviewStatus.All).longValue());
                Assert.assertEquals(1, counts.get(OverviewStatus.Review).longValue());
                Assert.assertEquals(2, counts.get(OverviewStatus.Validation).longValue());
            });
    }

    @Test
    public void fetchCoordinatorRequests() throws Exception {
        initMocks();
        initFetchTests();

        // Fetch requests with status 'Validation' for coordinator 1: should return 1 request
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator1))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
            }
        });

        // Fetch requests with status 'Validation' for coordinator 2: should return 3 requests
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator2))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(3, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
            }
        });

        // Fetch requests with status 'Validation' for coordinator 2, organisation 2: should return 2 requests
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/status/Validation/organisation/" + organisationUuid2.toString() + "/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator2))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(2, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(OverviewStatus.Validation, req.getStatus());
            }
        });
    }

    @Test
    public void fetchCoordinatorAndReviewerRequestCounts() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> requests = fetchAllForRole(requester, AuthorityConstants.RESEARCHER);
        Assert.assertEquals(3, requests.size());

        requests.stream().filter(req ->
            req.getOrganisations().stream().anyMatch(org -> org.getUuid().equals(organisationUuid2))
        ).forEach(request -> {
            try {
                validateRequest(request, coordinator2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Fetch request counts for coordinator
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/counts/coordinator",
                null,
                Collections.emptyMap())
                .with(token(coordinator2))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Coordinator counts: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                Map<OverviewStatus, Long> counts =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), countsTypeReference);
                Assert.assertEquals(3, counts.get(OverviewStatus.All).longValue());
                Assert.assertEquals(2, counts.get(OverviewStatus.Review).longValue());
                Assert.assertEquals(1, counts.get(OverviewStatus.Validation).longValue());
            });

        // Fetch request counts for reviewer
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/counts/reviewer",
                null,
                Collections.emptyMap())
                .with(token(reviewer2))
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Reviewer counts: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                Map<OverviewStatus, Long> counts =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), countsTypeReference);
                Assert.assertEquals(2, counts.get(OverviewStatus.All).longValue());
                Assert.assertEquals(2, counts.get(OverviewStatus.Review).longValue());
            });

    }

    @Test
    public void fetchReviewerRequests() throws Exception {
        initMocks();
        initFetchTests();

        List<RequestRepresentation> coordinatorRequests = fetchAllForCoordinator(coordinator1);
        Assert.assertThat(coordinatorRequests.size(), greaterThan(0));

        UUID requestUuid = coordinatorRequests.get(0).getUuid();

        // Submit for review
        performProcessAction(coordinator1, ACTION_VALIDATE, requestUuid, HttpMethod.GET, null);

        // Fetch requests with status 'Review' for reviewer 1: should return 1 request
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/reviewer",
                null,
                Collections.emptyMap())
                .with(token(reviewer1))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andDo(result -> {
            log.info("Result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            List<RequestRepresentation> requests =
                mapper.readValue(result.getResponse().getContentAsByteArray(), listTypeReference);
            Assert.assertEquals(1, requests.size());
            for(RequestRepresentation req: requests) {
                Assert.assertEquals(requestUuid, req.getUuid());
                Assert.assertEquals(OverviewStatus.Review, req.getStatus());
            }
        });
    }

    @Test
    public void submitInvalidDraftRejected() throws Exception {
        RequestRepresentation request = newDraft(requester);

        setRequestData(request);

        List<OrganisationRepresentation> organisations = new ArrayList<>();
        OrganisationRepresentation organisation = new OrganisationRepresentation();
        organisation.setUuid(organisationUuid1);
        organisations.add(organisation);
        request.setOrganisations(organisations);

        request.getRequestDetail().setTitle(""); // invalid, should be rejected.

        request = updateDraft(requester, request);
        Assert.assertEquals(1, request.getOrganisations().size());
        Assert.assertEquals("", request.getRequestDetail().getTitle());

        // Submit the draft. One request should have been generated (and is returned).
        mockMvc.perform(
            getRequest(HttpMethod.GET,
                REQUESTS_ROUTE + "/drafts/" + request.getUuid().toString() + "/submit",
                null,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isBadRequest())
        .andDo(result -> {
            log.info("Submitted result: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
        });
    }

    @Test
    public void rejectRequestFromValidation() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        MessageRepresentation rejectionMessage = new MessageRepresentation();
        rejectionMessage.setSummary("Test rejection");

        // Reject the request.
        ResultActions rejectedRequest
            = performProcessAction(coordinator1, ACTION_REJECT, requestRepresentation.getUuid(), HttpMethod.POST, rejectionMessage);

        rejectedRequest
            .andDo(result -> {
                log.info("Result rejected request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Rejected, requestResult.getStatus());
            })
            .andExpect(status().isOk());
    }

    @Test
    public void rejectRequestFromReview() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        // Send for review
        ResultActions validatedRequest
            = performProcessAction(coordinator1, ACTION_VALIDATE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        validatedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Review, requestResult.getStatus());
            });

        MessageRepresentation rejectionMessage = new MessageRepresentation();
        rejectionMessage.setSummary("Test rejection");

        // Reject the request.
        ResultActions rejectedRequest
            = performProcessAction(coordinator1, ACTION_REJECT, requestRepresentation.getUuid(), HttpMethod.POST, rejectionMessage);

        rejectedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result rejected request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);

                Assert.assertEquals(OverviewStatus.Rejected, requestResult.getStatus());
            });
    }

    @Test
    public void validateRequest() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        // Send for review
        ResultActions validatedRequest
            = performProcessAction(coordinator1, ACTION_VALIDATE, requestRepresentation.getUuid(), HttpMethod.GET, null);

        validatedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result validated request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Review, requestResult.getStatus());

                // Expect one review round to have been created
                Assert.assertNotNull(requestResult.getReviewRound());
                Assert.assertNull(requestResult.getReviewRound().getEndDate());
            });
    }

    @Test
    public void approveReviewRequest() throws Exception {
        initMocks();
        RequestRepresentation request = getSubmittedDraft();

        request = validateRequest(request, coordinator1);

        approveRequest(request, coordinator1);
    }

    @Test
    public void closeApprovedRequest() throws Exception {
        initMocks();
        RequestRepresentation request = getSubmittedDraft();

        request = validateRequest(request, coordinator1);

        approveRequest(request, coordinator1);

        // Close the request.
        MessageRepresentation message = new MessageRepresentation();
        message.setSummary("Approved, but no delivery");
        ResultActions res
            = performProcessAction(coordinator1, ACTION_CLOSE, request.getUuid(), HttpMethod.POST, message);

        res
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result closed request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Closed_Approved, requestResult.getStatus());
            });

        Thread.sleep(1000);

        // Verify that the requester is notified that the request is closed.
        verify(this.mailService).sendRequestClosedNotificationToRequester(any(), any());
    }

    @Test
    public void sendRequestForRevisionFromValidation() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft();

        MessageRepresentation revisionMessage = new MessageRepresentation();
        revisionMessage.setSummary("Test revision. Please change fields xyz");

        // Send for revision
        ResultActions revisedRequest
            = performProcessAction(coordinator1, ACTION_REQUEST_REVISION, requestRepresentation.getUuid(), HttpMethod.POST, revisionMessage);

        revisedRequest
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result revised request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Revision, requestResult.getStatus());
            });
    }

    @Test
    public void submitReviewFeedback() throws Exception {
        initMocks();
        RequestRepresentation request = getSubmittedDraft();

        // Send for review
        request = validateRequest(request, coordinator1);

        Assert.assertNotNull(request.getReviewRound());
        ReviewRoundRepresentation reviewRound = request.getReviewRound();

        ReviewFeedbackRepresentation reviewFeedback = reviewRound.getReviewFeedback().stream()
            .filter(feedback -> {
                log.warn("FEEDBACK: {}", feedback);
                log.warn("REVIEWER: {}", feedback.getReviewer());
                log.warn("REVIEWER UUID: {}", feedback.getReviewer().getUuid());
                return feedback.getReviewer().getUuid().equals(reviewerUuid1);
            })
            .findFirst().get();

        // Submit a review
        ReviewFeedbackRepresentation reviewFeedbackBody = new ReviewFeedbackRepresentation();
        reviewFeedbackBody.setUuid(reviewFeedback.getUuid());
        reviewFeedbackBody.setAdvice(ReviewProcessOutcome.Approved);
        MessageRepresentation approveMessage = new MessageRepresentation();
        approveMessage.setSummary("Excellent request!");
        approveMessage.setDescription("I appreciate the request and recommend approval.");
        reviewFeedbackBody.setMessage(approveMessage);

        performProcessAction(reviewer1, ACTION_SUBMIT_REVIEW_FEEDBACK, request.getUuid(), HttpMethod.PUT, reviewFeedbackBody)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result of submitting feedback: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                // result still contains one review round
                Assert.assertNotNull(requestResult.getReviewRound());
                // the review round contains the submitted feedback
                Assert.assertTrue(requestResult.getReviewRound().getReviewFeedback().stream().anyMatch(feedback ->
                    feedback.getUuid().equals(reviewFeedback.getUuid())
                ));
                // the submitted feedback has advice value 'Approved'
                requestResult.getReviewRound().getReviewFeedback().stream().forEach(feedback -> {
                    if (feedback.getUuid().equals(reviewFeedback.getUuid())) {
                        Assert.assertEquals(ReviewProcessOutcome.Approved, feedback.getAdvice());
                    }
                });
            });

        // Check that resubmitting a review results in an error
        reviewFeedbackBody.setAdvice(ReviewProcessOutcome.Rejected);
        MessageRepresentation rejectMessage = new MessageRepresentation();
        rejectMessage.setSummary("Ridiculous request!");
        rejectMessage.setDescription("I don't like the request at all and recommend rejection.");
        reviewFeedbackBody.setMessage(rejectMessage);
        performProcessAction(reviewer1, ACTION_SUBMIT_REVIEW_FEEDBACK, request.getUuid(), HttpMethod.PUT, reviewFeedbackBody)
            .andExpect(status().is4xxClientError())
            .andDo(result ->
                log.info("Result of resubmitting feedback: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString())
            );

        // Checking that the review is correctly persisted
        Request req = requestRepository.findOneByUuid(request.getUuid());

        List<ReviewRound> reviewRounds = reviewRoundRepository.findAllByRequestUuid(request.getUuid());
        Assert.assertThat(reviewRounds, hasSize(1));
        // the review round contains the submitted feedback
        List<ReviewFeedback> reviewFeedbackList = reviewFeedbackRepository.findAllByReviewRoundUuid(reviewRounds.get(0).getUuid());
        Assert.assertTrue(reviewFeedbackList.stream().anyMatch(feedback ->
            feedback.getUuid().equals(reviewFeedback.getUuid())
        ));
        // the submitted feedback has advice value 'Approved'
        reviewFeedbackList.stream().forEach(feedback -> {
            if (feedback.getUuid().equals(reviewFeedback.getUuid())) {
                Assert.assertEquals(ReviewProcessOutcome.Approved, feedback.getAdvice());
            }
        });
    }

    @Test
    public void submitReviewFeedbackForWrongUser() throws Exception {
        initMocks();
        RequestRepresentation request = getSubmittedDraft();

        // Send for review
        request = validateRequest(request, coordinator1);

        Assert.assertNotNull(request.getReviewRound());
        ReviewRoundRepresentation reviewRound = request.getReviewRound();

        ReviewFeedbackRepresentation reviewFeedback = reviewRound.getReviewFeedback().stream()
            .filter(feedback -> feedback.getReviewer().getUuid().equals(reviewerUuid1))
            .findFirst().get();

        // Submit a review for reviewer 1 by reviewer 2; should fail
        ReviewFeedbackRepresentation reviewFeedbackBody = new ReviewFeedbackRepresentation();
        reviewFeedbackBody.setUuid(reviewFeedback.getUuid());
        reviewFeedbackBody.setAdvice(ReviewProcessOutcome.Approved);
        MessageRepresentation approveMessage = new MessageRepresentation();
        approveMessage.setSummary("Excellent request!");
        approveMessage.setDescription("I appreciate the request and recommend approval.");
        reviewFeedbackBody.setMessage(approveMessage);

        performProcessAction(reviewer2, ACTION_SUBMIT_REVIEW_FEEDBACK, request.getUuid(), HttpMethod.PUT, reviewFeedbackBody)
            .andExpect(status().is4xxClientError())
            .andDo(result -> {
                log.info("Result of submitting feedback by wrong user: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
            });

    }

    @Test
    public void acceptExternalRequest() throws Exception {
        initMocks();

        // Create ext req data
        ExternalRequestRepresentation externalRequestRepresentation = new ExternalRequestRepresentation();
        externalRequestRepresentation.setUrl("http://test.url");
        externalRequestRepresentation.setHumanReadable("This is a test search query for external requests");
        externalRequestRepresentation.setNToken("nToken1");

        Map<String, String> collect1 = new HashMap<>();

        collect1.put("collectionID", organisationUuid1.toString() );
        collect1.put("biobankID", "bbmri-eric:biobankID:BE_B0383");

        ArrayList<Map<String, String>> collections = new ArrayList<>();
        collections.add(collect1);
        externalRequestRepresentation.setCollections(collections);

        // Submit ext req
        ResultActions externalRequest = mockMvc.perform(
            getRequest(HttpMethod.POST,
                "/api/requests/external/new",
                externalRequestRepresentation,
                Collections.emptyMap())
                .with(token(requester))
                .accept(MediaType.APPLICATION_JSON));

        externalRequest
            .andDo(result -> {
                log.info("Result rejected request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(OverviewStatus.Draft, requestResult.getStatus());
                Assert.assertEquals("This is a test search query for external requests",
                    requestResult.getRequestDetail().getSearchQuery());
            });
    }

}
