/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.test;

import nl.thehyve.podium.common.security.UserAuthenticationToken;
import org.mockito.BDDMockito;
import org.mockito.internal.util.collections.Sets;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.given;

/**
 * A bean providing simple mocking of OAuth2 access tokens for security integration tests.
 */
@Profile("test")
@Component
@Profile("test")
public class OAuth2TokenMockUtil {

    @MockBean
    private ResourceServerTokenServices tokenServices;

    private OAuth2Authentication createAuthentication(String username, Set<String> scopes, Set<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        User principal = new User(username, "test", true, true, true, true, authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(),
            principal.getAuthorities());

        // Create the authorization request and OAuth2Authentication object
        OAuth2Request authRequest = new OAuth2Request(null, "testClient", null, true, scopes, null, null, null,
            null);
        return new OAuth2Authentication(authRequest, authentication);
    }

    public RequestPostProcessor oauth2Authentication(String username, Set<String> scopes, Set<String> roles) {
        String uuid = String.valueOf(UUID.randomUUID());

        given(tokenServices.loadAuthentication(uuid))
            .willReturn(createAuthentication(username, scopes, roles));

        given(tokenServices.readAccessToken(uuid)).willReturn(new DefaultOAuth2AccessToken(uuid));

        return new OAuth2PostProcessor(uuid);
    }

    public RequestPostProcessor oauth2Authentication(String username, Set<String> scopes) {
        return oauth2Authentication(username, scopes, Collections.emptySet());
    }

    public RequestPostProcessor oauth2Authentication(String username) {
        return oauth2Authentication(username, Collections.emptySet());
    }

    private OAuth2Authentication createAuthentication(UserAuthenticationToken authentication) {
        authentication.setAuthenticated(true);

        Set<String> scopes = Sets.newSet("some-client");
        // Create the authorization request and OAuth2Authentication object
        OAuth2Request authRequest = new OAuth2Request(null, "testClient", null, true, scopes, null, null, null,
            null);
        return new OAuth2Authentication(authRequest, authentication);
    }

    public RequestPostProcessor oauth2Authentication(UserAuthenticationToken authentication) {
        String uuid = String.valueOf(UUID.randomUUID());

        given(tokenServices.loadAuthentication(uuid))
            .willReturn(createAuthentication(authentication));

        given(tokenServices.readAccessToken(uuid)).willReturn(new DefaultOAuth2AccessToken(uuid));

        return new OAuth2PostProcessor(uuid);
    }

    public static class OAuth2PostProcessor implements RequestPostProcessor {

        private String token;

        public OAuth2PostProcessor(String token) {
            this.token = token;
        }

        @Override
        public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
            mockHttpServletRequest.addHeader("Authorization", "Bearer " + token);

            return mockHttpServletRequest;
        }
    }
}
