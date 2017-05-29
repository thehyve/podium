/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.exceptions.ActionNotAllowedInStatus;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.RequestReviewProcess;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Service tests for the {@link RequestReviewProcessService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
@Transactional
public class RequestReviewProcessServiceIntTest {

    @Autowired
    RequestReviewProcessService requestReviewProcessService;

    private UUID testUuid = UUID.randomUUID();

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(authenticatedUser.getUuid()).thenReturn(testUuid);
    }

    @Test
    public void testStart() {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        Assert.assertEquals(RequestReviewStatus.Validation, requestReviewProcess.getStatus());
    }

    @Test
    public void testToReviewAfterValidation() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.submitForReview(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Review, requestReviewProcess.getStatus());
    }

    @Test
    public void testToRevisionAfterValidation() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Revision, requestReviewProcess.getStatus());
    }

    @Test
    public void testRejectAfterValidation() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.reject(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Closed, requestReviewProcess.getStatus());
        Assert.assertEquals(ReviewProcessOutcome.Rejected, requestReviewProcess.getDecision());
    }

    @Test(expected = ActionNotAllowedInStatus.class)
    public void testApproveAfterValidationNotAllowed() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcessService.approve(authenticatedUser, requestReviewProcess);
    }

    @Test
    public void testRejectAfterReview() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.submitForReview(authenticatedUser, requestReviewProcess);
        requestReviewProcess = requestReviewProcessService.reject(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Closed, requestReviewProcess.getStatus());
        Assert.assertEquals(ReviewProcessOutcome.Rejected, requestReviewProcess.getDecision());
    }

    @Test
    public void testRevisionAfterReview() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.submitForReview(authenticatedUser, requestReviewProcess);
        requestReviewProcess = requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Revision, requestReviewProcess.getStatus());
    }

    @Test
    public void testValidationAfterRevision() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Revision, requestReviewProcess.getStatus());
        requestReviewProcess = requestReviewProcessService.submitForValidation(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Validation, requestReviewProcess.getStatus());
    }

    @Test(expected = ActionNotAllowedInStatus.class)
    public void testRevisionAfterRevisionNotAllowed() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
        requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
    }

    @Test(expected = ActionNotAllowedInStatus.class)
    public void testReviewAfterRevisionNotAllowed() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.requestRevision(authenticatedUser, requestReviewProcess);
        requestReviewProcessService.submitForReview(authenticatedUser, requestReviewProcess);
    }

    @Test
    public void testApproveAfterReview() throws ActionNotAllowedInStatus {
        RequestReviewProcess requestReviewProcess = requestReviewProcessService.start(authenticatedUser);
        requestReviewProcess = requestReviewProcessService.submitForReview(authenticatedUser, requestReviewProcess);
        requestReviewProcess = requestReviewProcessService.approve(authenticatedUser, requestReviewProcess);
        Assert.assertEquals(RequestReviewStatus.Closed, requestReviewProcess.getStatus());
        Assert.assertEquals(ReviewProcessOutcome.Approved, requestReviewProcess.getDecision());
    }

}
