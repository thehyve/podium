/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.domain.ReviewFeedback;
import nl.thehyve.podium.domain.ReviewRound;
import nl.thehyve.podium.repository.ReviewFeedbackRepository;
import nl.thehyve.podium.repository.ReviewRoundRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestReviewResource REST controller.
 *
 * @see RequestReviewResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestReviewResourceIntTest extends AbstractRequestDataIntTest {

    @Autowired
    private ReviewRoundRepository reviewRoundRepository;

    @Autowired
    private ReviewFeedbackRepository reviewFeedbackRepository;

    @Test
    public void rejectRequestFromValidation() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation requestRepresentation = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation requestRepresentation = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation request = getSubmittedDraft(requester, organisationUuid1);

        request = validateRequest(request, coordinator1);

        approveRequest(request, coordinator1);
    }

    @Test
    public void closeApprovedRequest() throws Exception {
        initMocks();
        RequestRepresentation request = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation requestRepresentation = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation request = getSubmittedDraft(requester, organisationUuid1);

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
        RequestRepresentation request = getSubmittedDraft(requester, organisationUuid1);

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

}
