/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.config;

import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.CustomUserAuthenticationConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.servlet.http.HttpServletResponse;
import java.security.KeyPair;

@Configuration
@EnableAuthorizationServer
public class UaaConfiguration extends AuthorizationServerConfigurerAdapter {

    @EnableResourceServer
    public static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Autowired
        TokenStore tokenStore;

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            .and()
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
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/verify").permitAll()
                .antMatchers("/api/reverify").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/account/reset_password/init").permitAll()
                .antMatchers("/api/account/reset_password/finish").permitAll()
                .antMatchers("/api/profile-info").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/**").hasAuthority(AuthorityConstants.PODIUM_ADMIN)
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/index.html").hasAuthority(AuthorityConstants.PODIUM_ADMIN);
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("podium-uaa").tokenStore(tokenStore);
        }
    }

    @Autowired
    private PodiumProperties podiumProperties;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        /*
        For a better client design, this should be done by a ClientDetailsService (similar to UserDetailsService).
         */
        clients.inMemory()
            .withClient("web_app")
            .scopes("openid")
            .autoApprove(true)
            .authorizedGrantTypes("implicit","refresh_token", "password", "authorization_code")
            .and()
            .withClient(podiumProperties.getSecurity().getClientAuthorization().getClientId())
            .secret(podiumProperties.getSecurity().getClientAuthorization().getClientSecret())
            .scopes("web-app")
            .autoApprove(true)
            .authorizedGrantTypes("client_credentials");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
            .authenticationManager(authenticationManager)
            .accessTokenConverter(jwtAccessTokenConverter());
    }

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    /**
     * Apply the token converter (and enhancer) for token store.
     *
     *  @return A JWTTokenStore using the `jwtAccessTokenConverter`.
     */
    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
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

    /**
     * This bean generates an token enhancer, which manages the exchange between JWT access tokens and Authentication
     * in both direction.
     *
     * @return an access token converter configured with the authorization server's public/private keys
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
             new ClassPathResource("keystore.jks"), "password".toCharArray())
             .getKeyPair("selfsigned");
        converter.setKeyPair(keyPair);
        converter.setAccessTokenConverter(defaultAccessTokenConverter());
        return converter;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess(
                "isAuthenticated()");
    }
}
