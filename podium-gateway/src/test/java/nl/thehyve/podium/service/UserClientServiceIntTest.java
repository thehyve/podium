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
import nl.thehyve.podium.common.service.dto.UserRepresentation;
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

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Service tests for the {@link UserClientService}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {PodiumGatewayApp.class, SecurityBeanOverrideConfiguration.class})
public class UserClientServiceIntTest {

    private final Logger log = LoggerFactory.getLogger(UserClientServiceIntTest.class);

    @Autowired
    private UserClientService userClientService;

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
    public void testFetchUserByUuid() throws JsonProcessingException {
        log.info("Testing with mock port {}.", wireMockRule.port());

        UUID userUuid = UUID.randomUUID();
        UserRepresentation mockUser = new UserRepresentation();
        mockUser.setLogin("mock");
        mockUser.setUuid(userUuid);

        stubFor(get(urlEqualTo("/internal/users/uuid/" + userUuid.toString()))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsString(mockUser))));

        UserRepresentation user = userClientService.findUserByUuid(userUuid);
        assertThat(user).isNotNull();
    }

}
