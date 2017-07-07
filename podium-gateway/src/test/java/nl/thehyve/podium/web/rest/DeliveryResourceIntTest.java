/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.*;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
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
public class DeliveryResourceIntTest extends AbstractRequestDataIntTest {

    @Test
    public void startDeliveryProcesses() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();

        Thread.sleep(1000);
        reset(this.auditService);

        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        Assert.assertNotNull(deliveryRequest);
        Assert.assertEquals(deliveryRequest.getStatus(), OverviewStatus.Delivery);

        Thread.sleep(1000);

        // One request update, two delivery process updates
        verify(this.auditService, times(3)).publishEvent(any());
    }

    private void testRelease(RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess) throws Exception {
        // Release
        DeliveryReferenceRepresentation reference = new DeliveryReferenceRepresentation();
        String downloadUrl = "http://example.com/downloadData";
        reference.setReference(downloadUrl);
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Released, resultDeliveryProcess.getStatus());
                Assert.assertEquals(downloadUrl, resultDeliveryProcess.getReference());
            });
    }

    @Test
    public void releaseDelivery() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();

        // Setup delivery request
        request = createDeliveryProcesses(request);

        // Fetch delivery processes
        ResultActions deliveryProcessesResult
            = performProcessAction(coordinator1, ACTION_GET_DELIVERIES, request.getUuid(), HttpMethod.GET, null);

        final List<DeliveryProcessRepresentation> deliveryProcesses = new ArrayList<>();

        deliveryProcessesResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery processes: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                deliveryProcesses.addAll(
                    mapper.readValue(result.getResponse().getContentAsByteArray(), deliveryProcessListTypeReference)
                );
            });

        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        testRelease(request, deliveryProcess);

        Thread.sleep(1000);

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryReleasedNotificationToRequester(any(), any(), any());
        // Test status update event
        verify(this.auditService, times(1)).publishEvent(any());
    }

    private void testReceived(RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess) throws Exception {
        // Received
        ResultActions receivedDeliveryResult
            = performDeliveryAction(requester, DELIVERY_RECEIVED, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.GET, null);

        receivedDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Closed, resultDeliveryProcess.getStatus());
                Assert.assertEquals(DeliveryProcessOutcome.Received, resultDeliveryProcess.getOutcome());
            });

    }

    @Test
    public void deliveryReceived() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        List<DeliveryProcessRepresentation> deliveryProcesses = getDeliveryProcesses(deliveryRequest);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        testRelease(request, deliveryProcess);

        Thread.sleep(1000);
        reset(this.auditService);

        testReceived(request, deliveryProcess);

        Thread.sleep(1000);

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryReceivedNotificationToCoordinators(any(), any(), any(), anyListOf(UserRepresentation.class));
        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());
    }

    private void testCancel(RequestRepresentation request, DeliveryProcessRepresentation deliveryProcess) throws Exception {
        // Cancel
        MessageRepresentation message = new MessageRepresentation();
        String summary = "Delivery cancelled";
        message.setSummary(summary);
        message.setDescription("Cancelled because materials were damaged.");
        ResultActions rejectDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_CANCEL, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, message);

        rejectDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Closed, resultDeliveryProcess.getStatus());
                Assert.assertEquals(DeliveryProcessOutcome.Cancelled, resultDeliveryProcess.getOutcome());
                List<PodiumEventRepresentation> events = resultDeliveryProcess.getHistoricEvents();
                Assert.assertNotEquals(0, events.size());
                events.forEach(event -> log.info("Event: {}", event));
                PodiumEventRepresentation latestEvent = events.get(events.size() - 1);
                Assert.assertEquals(summary, latestEvent.getData().get("messageSummary"));
            });
    }

    @Test
    public void cancelDeliveryAfterStart() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        List<DeliveryProcessRepresentation> deliveryProcesses = getDeliveryProcesses(deliveryRequest);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        testCancel(request, deliveryProcess);

        Thread.sleep(1000);

        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryCancelledNotificationToRequester(any(), any(), any(UserRepresentation.class));
    }

    @Test
    public void cancelReleasedDelivery() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        List<DeliveryProcessRepresentation> deliveryProcesses = getDeliveryProcesses(deliveryRequest);
        DeliveryProcessRepresentation deliveryProcess = deliveryProcesses.get(0);

        Thread.sleep(1000);
        reset(this.auditService);

        // Release
        DeliveryReferenceRepresentation reference = new DeliveryReferenceRepresentation();
        ResultActions releaseDeliveryResult
            = performDeliveryAction(coordinator1, DELIVERY_RELEASE, request.getUuid(), deliveryProcess.getUuid(), HttpMethod.POST, reference);

        releaseDeliveryResult
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result delivery process: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                DeliveryProcessRepresentation resultDeliveryProcess =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), DeliveryProcessRepresentation.class);
                Assert.assertEquals(DeliveryStatus.Released, resultDeliveryProcess.getStatus());
            });

        testCancel(request, deliveryProcess);

        Thread.sleep(1000);

        // Test status update events
        verify(this.auditService, times(2)).publishEvent(any());

        // Test if requester has been notified
        verify(this.mailService, times(1)).sendDeliveryCancelledNotificationToRequester(any(), any(), any(UserRepresentation.class));
    }

    private void testCloseRequest(RequestRepresentation request, OverviewStatus expectedOverviewStatus) throws Exception {
        // Close the request.
        MessageRepresentation message = new MessageRepresentation();
        message.setSummary("Closed request after delivery. Outcome: " + expectedOverviewStatus.name());
        ResultActions res
            = performProcessAction(coordinator1, "close", request.getUuid(), HttpMethod.POST, message);

        res
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result closed request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation requestResult =
                    mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                Assert.assertEquals(expectedOverviewStatus, requestResult.getStatus());
            });
    }

    @Test
    public void closeRequestAfterDeliveryCancel() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        List<DeliveryProcessRepresentation> deliveryProcesses = getDeliveryProcesses(deliveryRequest);

        for (DeliveryProcessRepresentation deliveryProcess: deliveryProcesses) {
            testCancel(request, deliveryProcess);
        }

        Thread.sleep(1000);
        reset(this.auditService);
        reset(this.mailService);

        testCloseRequest(request, OverviewStatus.Cancelled);

        Thread.sleep(1000);
        // Verify that the requester is notified that the request is closed.
        verify(this.mailService).sendRequestClosedNotificationToRequester(any(), any());
        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());
    }

    @Test
    public void closeRequestAfterDeliveryReceived() throws Exception {
        initMocks();
        RequestRepresentation request = getApprovedRequest();
        RequestRepresentation deliveryRequest = createDeliveryProcesses(request);
        List<DeliveryProcessRepresentation> deliveryProcesses = getDeliveryProcesses(deliveryRequest);

        for (DeliveryProcessRepresentation deliveryProcess: deliveryProcesses) {
            testRelease(request, deliveryProcess);

            Thread.sleep(1000);

            testReceived(request, deliveryProcess);
        }

        Thread.sleep(1000);
        reset(this.auditService);
        reset(this.mailService);

        testCloseRequest(request, OverviewStatus.Delivered);

        Thread.sleep(1000);
        // Verify that the requester is notified that the request is closed.
        verify(this.mailService).sendRequestClosedNotificationToRequester(any(), any());
        // Test status update events
        verify(this.auditService, times(1)).publishEvent(any());
    }

}
