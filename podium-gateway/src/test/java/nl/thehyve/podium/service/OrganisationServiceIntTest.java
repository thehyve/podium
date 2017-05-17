/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Service tests for the {@link OrganisationClientService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class OrganisationServiceIntTest {

    private final Logger log = LoggerFactory.getLogger(OrganisationServiceIntTest.class);

    @Autowired
    private OrganisationClientService organisationService;

    @Autowired
    private LoadBalancerClient loadBalancer;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));

    @Autowired
    @Qualifier("requestAuth2ClientContext")
    OAuth2ClientContext requestAuth2ClientContext;

    private static String tokenUuid = String.valueOf(UUID.randomUUID());

    private static OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(tokenUuid);

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        requestAuth2ClientContext.setAccessToken(accessToken);
        log.info("Load balancer: {}", loadBalancer);
        ServiceInstance service = loadBalancer.choose("podiumuaa");
        log.info("Service podiumuaa: {}", service);
        if (service != null) {
            log.info("Service details: host = {}, port = {}", service.getHost(), service.getPort());
        }
    }

    @Test
    public void testFetchAllOrganisations() throws URISyntaxException, JsonProcessingException {
        log.info("Testing with mock port {}.", wireMockRule.port());

        List<OrganisationDTO> mockOrganisations = new ArrayList<>();
        OrganisationDTO mockOrganisation = new OrganisationDTO();
        mockOrganisation.setName("Test organisation");
        mockOrganisations.add(mockOrganisation);

        stubFor(get(urlEqualTo("/api/organisations/all"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(mockOrganisations))));

        List<OrganisationDTO> organisations = organisationService.findAllOrganisations();
        assertThat(organisations).isNotEmpty();

        verify(1, getRequestedFor(urlEqualTo("/api/organisations/all")));
    }

}
