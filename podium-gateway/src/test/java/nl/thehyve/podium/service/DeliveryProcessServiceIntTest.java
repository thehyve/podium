/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import nl.thehyve.podium.domain.DeliveryProcess;
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
 * Service tests for the {@link DeliveryProcessService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
@Transactional
public class DeliveryProcessServiceIntTest {

    @Autowired
    DeliveryProcessService deliveryProcessService;

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
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        Assert.assertEquals(DeliveryStatus.Preparation, deliveryProcess.getStatus());
        Assert.assertEquals(RequestType.Data, deliveryProcess.getType());
    }

    @Test
    public void testToReleasedAfterPreparation() throws ActionNotAllowed {
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        deliveryProcess = deliveryProcessService.release(authenticatedUser, deliveryProcess);
        Assert.assertEquals(DeliveryStatus.Released, deliveryProcess.getStatus());
    }

    @Test
    public void testCancelAfterPreparation() throws ActionNotAllowed {
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        deliveryProcess = deliveryProcessService.cancel(authenticatedUser, deliveryProcess);
        Assert.assertEquals(DeliveryStatus.Closed, deliveryProcess.getStatus());
        Assert.assertEquals(DeliveryProcessOutcome.Cancelled, deliveryProcess.getOutcome());
    }

    @Test
    public void testToReceivedAfterReleased() throws ActionNotAllowed {
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        deliveryProcess = deliveryProcessService.release(authenticatedUser, deliveryProcess);
        deliveryProcess = deliveryProcessService.received(authenticatedUser, deliveryProcess);
        Assert.assertEquals(DeliveryStatus.Closed, deliveryProcess.getStatus());
        Assert.assertEquals(DeliveryProcessOutcome.Received, deliveryProcess.getOutcome());
    }

    @Test
    public void testCancelAfterReleased() throws ActionNotAllowed {
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        deliveryProcess = deliveryProcessService.release(authenticatedUser, deliveryProcess);
        deliveryProcess = deliveryProcessService.cancel(authenticatedUser, deliveryProcess);
        Assert.assertEquals(DeliveryStatus.Closed, deliveryProcess.getStatus());
        Assert.assertEquals(DeliveryProcessOutcome.Cancelled, deliveryProcess.getOutcome());
    }

    @Test(expected = ActionNotAllowed.class)
    public void testReceivedAfterStartNotAllowed() throws ActionNotAllowed {
        DeliveryProcess deliveryProcess = deliveryProcessService.start(authenticatedUser, RequestType.Data);
        deliveryProcessService.received(authenticatedUser, deliveryProcess);
    }

}
