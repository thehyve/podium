/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.service.dto.*;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the RequestTemplateResource REST controller.
 *
 * @see RequestTemplateResource
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SpringBootTest(classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class RequestTemplateResourceIntTest extends AbstractRequestDataIntTest {

    RequestTemplateRepresentation createTestTemplate() {
        // Create request template data
        RequestTemplateRepresentation requestTemplateRepresentation = new RequestTemplateRepresentation();
        requestTemplateRepresentation.setUrl("http://test.url");
        requestTemplateRepresentation.setHumanReadable("This is a test search query for external requests");
        requestTemplateRepresentation.setNToken("nToken1");

        Map<String, String> collect1 = new HashMap<>();

        collect1.put("biobankID", organisationUuid1.toString());
        collect1.put("collectionID", "bbmri-eric:biobankID:BE_B0383");

        Map<String, String> collect2 = new HashMap<>();

        collect2.put("biobankID", "bbmri-eric:biobankID:BE_B0383");
        collect2.put("collectionID", organisationUuid1.toString());

        ArrayList<Map<String, String>> collections = new ArrayList<>();
        collections.add(collect1);
        collections.add(collect2);
        requestTemplateRepresentation.setCollections(collections);
        return requestTemplateRepresentation;
    }

    @Test
    public void createRequestTemplate() throws Exception {
        initMocks();

        RequestTemplateRepresentation requestTemplateRepresentation = createTestTemplate();
        String authentication = "test:test";

        // Perform a POST request to create the request template
        URI viewUri = createRequestTemplate(requestTemplateRepresentation, authentication);

        // Try to retrieve the request template
        log.info("View URI: {}", viewUri);
        String[] queryParts = viewUri.toASCIIString().split("=");
        UUID templateUuid = UUID.fromString(queryParts[queryParts.length - 1]);

        RequestTemplateRepresentation result = getRequestTemplate(requester, templateUuid);
        Assert.assertEquals(requestTemplateRepresentation.getUrl(), result.getUrl());
        Assert.assertEquals(requestTemplateRepresentation.getHumanReadable(), result.getHumanReadable());
        Assert.assertThat(result.getOrganisations(), contains(organisationUuid1));
    }

    @Test
    public void accessDeniedWithUnknownToken() throws Exception {
        initMocks();

        RequestTemplateRepresentation requestTemplateRepresentation = createTestTemplate();
        String authentication = "invalid:invalid";

        // Perform a POST request to create the request template
        performCreateRequestTemplate(requestTemplateRepresentation, authentication, true)
            .andExpect(status().isForbidden());
    }

    @Test
    public void accessDeniedWithInvalidToken() throws Exception {
        initMocks();

        RequestTemplateRepresentation requestTemplateRepresentation = createTestTemplate();
        String authentication = "Invalid authentication string!";

        // Perform a POST request to create the request template
        performCreateRequestTemplate(requestTemplateRepresentation, authentication, false)
            .andExpect(status().isForbidden());
    }

}
