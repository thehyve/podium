/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.config;

import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.CustomUserAuthenticationConverter;
import nl.thehyve.podium.security.CustomAuthenticationProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class MicroserviceSecurityConfiguration extends ResourceServerConfigurerAdapter {

    private final PodiumProperties podiumProperties;

    private final DiscoveryClient discoveryClient;

    public MicroserviceSecurityConfiguration(PodiumProperties podiumProperties,
            DiscoveryClient discoveryClient) {

        this.podiumProperties = podiumProperties;
        this.discoveryClient = discoveryClient;
    }

    @Bean
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return new CustomAuthenticationProvider();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .csrf()
            .disable()
            .headers()
            .frameOptions()
            .disable()
        .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            .authorizeRequests()
            .antMatchers("/api/profile-info").permitAll()
            .antMatchers("/api/**").authenticated()
            .antMatchers("/management/**").hasAuthority(AuthorityConstants.PODIUM_ADMIN)
            .antMatchers("/swagger-resources/configuration/ui").permitAll();
    }

    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    @Bean
    public CustomUserAuthenticationConverter customUserAuthenticationConverter() {
        return new CustomUserAuthenticationConverter();
    }

    @Bean
    public DefaultAccessTokenConverter defaultAccessTokenConverter() {
        DefaultAccessTokenConverter defaultAccessTokenConverter = new DefaultAccessTokenConverter();
        defaultAccessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter());
        return defaultAccessTokenConverter;
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(
            @Qualifier("loadBalancedRestTemplate") RestTemplate keyUriRestTemplate) {

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getKeyFromAuthorizationServer(keyUriRestTemplate));
        converter.setAccessTokenConverter(defaultAccessTokenConverter());
        return converter;
    }

    @Bean
    public RestTemplate loadBalancedRestTemplate(RestTemplateCustomizer customizer) {
        RestTemplate restTemplate = new RestTemplate();
        customizer.customize(restTemplate);
        return restTemplate;
    }

    private String getKeyFromAuthorizationServer(RestTemplate keyUriRestTemplate) {
        // Load available UAA servers
        discoveryClient.getServices();

        HttpEntity<Void> request = new HttpEntity<Void>(new HttpHeaders());
        return (String) keyUriRestTemplate
            .exchange("http://podiumUaa/oauth/token_key", HttpMethod.GET, request, Map.class).getBody()
            .get("value");
    }

}
