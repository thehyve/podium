/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.config.SecurityBeanOverrideConfiguration;

import nl.thehyve.podium.security.OAuth2TokenMockUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.*;

/**
 * Service tests for the {@link OrganisationService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class},
    properties = {
        "podiumuaa.ribbon.listOfServers=localhost:9001",
        "eureka.client.enabled=false",
        "feign.hystrix.enabled=false"})
public class OrganisationServiceIntTest {

    private final Logger log = LoggerFactory.getLogger(OrganisationServiceIntTest.class);

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private RestTemplate restTemplate;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));

    @Autowired
    @Qualifier("requestAuth2ClientContext")
    OAuth2ClientContext requestAuth2ClientContext;

    @Autowired
    BaseLoadBalancer baseLoadBalancer;

    private static String tokenUuid = String.valueOf(UUID.randomUUID());

    private static OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(tokenUuid);

    @Before
    public void setup() {
        requestAuth2ClientContext.setAccessToken(accessToken);
        log.info("Load balancer: {}", baseLoadBalancer);
    }

    @Test
    public void testFetchAllOrganisations() throws URISyntaxException {
        log.info("Testing with mock port {}.", wireMockRule.port());

        stubFor(get(urlEqualTo("/podiumuaa/api/organisations/"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("[]")));
        List<OrganisationDTO> organisations = organisationService.findAllOrganisations();
        assertThat(organisations).isNotEmpty();
    }

}
