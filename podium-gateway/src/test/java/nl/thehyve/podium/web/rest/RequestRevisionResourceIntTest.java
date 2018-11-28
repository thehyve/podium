/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestRevisionResource REST controller.
 *
 * @see RequestRevisionResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestRevisionResourceIntTest extends AbstractRequestDataIntTest {

    @Test
    public void reviseAndResubmitRequest() throws Exception {
        initMocks();
        // Setup a submitted draft
        RequestRepresentation requestRepresentation = getSubmittedDraft(requester, organisationUuid1);

        // Request revision
        MessageRepresentation revisionMessage = new MessageRepresentation();
        revisionMessage.setSummary("Test revision. Please change fields xyz");
        RequestRepresentation revisedRequest = requestRevision(requestRepresentation, coordinator1, revisionMessage);
        Assert.assertEquals(OverviewStatus.Revision, revisedRequest.getStatus());

        // Fetch the request as requester
        revisedRequest = fetchRequest(requester, "/" + revisedRequest.getUuid().toString());
        Assert.assertEquals(OverviewStatus.Revision, revisedRequest.getStatus());
        Assert.assertNotNull(revisedRequest.getRevisionDetail());

        // Update request, the changes are stored in the revision details object
        String backgroundText = "More background about the research.";
        revisedRequest.getRevisionDetail().setBackground(backgroundText);
        revisedRequest = updateRevision(requester, revisedRequest);
        Assert.assertEquals(revisedRequest.getRevisionDetail().getBackground(), backgroundText);

        // Resubmit revised request
        performProcessAction(requester, ACTION_SUBMIT_REVISION, revisedRequest.getUuid(), HttpMethod.GET, null)
            .andExpect(status().isOk())
            .andDo(result -> {
                log.info("Result revised request: {} ({})", result.getResponse().getStatus(), result.getResponse().getContentAsString());
                RequestRepresentation request = mapper.readValue(result.getResponse().getContentAsByteArray(), RequestRepresentation.class);
                // The request should have status Validation
                Assert.assertEquals(OverviewStatus.Validation, request.getStatus());
                // The revised fields should be stored in the request details
                Assert.assertEquals(backgroundText, request.getRequestDetail().getBackground());
            });
    }

}
