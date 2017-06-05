/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.client;

import feign.RequestInterceptor;
import io.github.jhipster.security.uaa.LoadBalancedResourceDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2ClientContext;

import javax.enterprise.context.RequestScoped;
import java.io.IOException;

@Configuration
public class OAuth2InterceptedFeignConfiguration {

    private final Logger log = LoggerFactory.getLogger(OAuth2InterceptedFeignConfiguration.class);

    private final LoadBalancedResourceDetails loadBalancedResourceDetails;

    @Autowired
    @Qualifier("requestAuth2ClientContext")
    OAuth2ClientContext requestAuth2ClientContext;

    public OAuth2InterceptedFeignConfiguration(LoadBalancedResourceDetails loadBalancedResourceDetails) {
        this.loadBalancedResourceDetails = loadBalancedResourceDetails;
    }

    @Profile("!test")
    @RequestScoped
    @Bean(name = "oauth2RequestInterceptor")
    public RequestInterceptor getOAuth2RequestInterceptor() throws IOException {
        log.info("Creating new request interceptor with context {} and resource {}", requestAuth2ClientContext, loadBalancedResourceDetails);
        return new OAuth2FeignRequestInterceptor(requestAuth2ClientContext, loadBalancedResourceDetails);
    }
}
