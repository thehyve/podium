/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import nl.thehyve.podium.PodiumGatewayApp;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.common.security.SerialisedUser;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Service tests for the {@link AuditService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class AuditServiceIntTest {

    private static String tokenUuid = String.valueOf(UUID.randomUUID());

    private static OAuth2AccessToken accessToken = new DefaultOAuth2AccessToken(tokenUuid);

    private final Logger log = LoggerFactory.getLogger(AuditServiceIntTest.class);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(8089));

    @Autowired
    @Qualifier("requestAuth2ClientContext")
    OAuth2ClientContext requestAuth2ClientContext;

    @Autowired
    private AuditService auditService;

    @Autowired
    private LoadBalancerClient loadBalancer;

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
    public void testPublishEvent() throws URISyntaxException, JsonProcessingException, InterruptedException {
        log.info("Testing with mock port {}.", wireMockRule.port());

        UUID requestUuid = UUID.randomUUID();

        UUID userUuid = UUID.randomUUID();
        SerialisedUser mockUser = new SerialisedUser(userUuid, "mock", Arrays.asList("ROLE_RESEARCHER"), new HashMap<>());

        StatusUpdateEvent mockEvent = new StatusUpdateEvent(mockUser, RequestReviewStatus.Review, RequestReviewStatus.Revision, requestUuid);

        stubFor(post(urlEqualTo("/internal/audit/events"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.CREATED.value())));

        auditService.publishEvent(mockEvent);

        Thread.sleep(1000);

        verify(1, postRequestedFor(urlEqualTo("/internal/audit/events")));
    }

}
