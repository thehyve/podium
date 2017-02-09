/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package org.bbmri.podium.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.net.URI;
import java.net.URISyntaxException;

public class LoadBalancedResourceDetails extends ClientCredentialsResourceDetails {

    Logger log = LoggerFactory.getLogger(LoadBalancedResourceDetails.class);

    private String tokenServiceId;

    private LoadBalancerClient loadBalancerClient;

    private PodiumProperties podiumProperties;

    @Autowired(required = false)
    public void setLoadBalancerClient(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public String getAccessTokenUri() {
        if (loadBalancerClient != null && tokenServiceId != null && !tokenServiceId.isEmpty()) {
            try {
                return loadBalancerClient.reconstructURI(
                    loadBalancerClient.choose(tokenServiceId),
                    new URI(super.getAccessTokenUri())
                ).toString();
            } catch (URISyntaxException e) {
                log.error("{}: {}", e.getClass().toString(), e.getMessage());

                return super.getAccessTokenUri();
            }
        } else {
            return super.getAccessTokenUri();
        }
    }

    public String getTokenServiceId() {
        return this.tokenServiceId;
    }

    public void setTokenServiceId(String tokenServiceId) {
        this.tokenServiceId = tokenServiceId;
    }
}
