/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.config.apidoc;

import com.fasterxml.classmate.TypeResolver;
import io.github.jhipster.config.apidoc.PageableParameterBuilderPlugin;
import nl.thehyve.podium.common.config.PodiumConstants;
import nl.thehyve.podium.common.config.PodiumProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Springfox Swagger configuration.
 *
 * Warning! When having a lot of REST endpoints, Springfox can become a performance issue. In that
 * case, you can use a specific Spring profile for this class, so that only front-end developers
 * have access to the Swagger view.
 */
@Configuration
@ConditionalOnClass({ ApiInfo.class, BeanValidatorPluginsConfiguration.class })
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@Profile(PodiumConstants.SPRING_PROFILE_SWAGGER)
public class SwaggerConfiguration {

    private final Logger log = LoggerFactory.getLogger(SwaggerConfiguration.class);

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    private final PodiumProperties podiumProperties;

    public SwaggerConfiguration(PodiumProperties podiumProperties) {
        this.podiumProperties = podiumProperties;
    }

    /**
     * Swagger Springfox configuration.
     *
     * @return the Swagger Springfox configuration
     */
    @Bean
    public Docket swaggerSpringfoxApiDocket() {
        log.debug("Starting Swagger");
        StopWatch watch = new StopWatch();
        watch.start();
        Contact contact = new Contact(
            podiumProperties.getSwagger().getContactName(),
            podiumProperties.getSwagger().getContactUrl(),
            podiumProperties.getSwagger().getContactEmail());

        ApiInfo apiInfo = new ApiInfo(
            podiumProperties.getSwagger().getTitle(),
            podiumProperties.getSwagger().getDescription(),
            podiumProperties.getSwagger().getVersion(),
            podiumProperties.getSwagger().getTermsOfServiceUrl(),
            contact,
            podiumProperties.getSwagger().getLicense(),
            podiumProperties.getSwagger().getLicenseUrl(),
            new ArrayList<>());

        String host = podiumProperties.getSwagger().getHost();
        String[] protocols = podiumProperties.getSwagger().getProtocols();

        Docket docket = new Docket(DocumentationType.SWAGGER_2)
            .host(host)
            .protocols(new HashSet<>(Arrays.asList(protocols)))
            .apiInfo(apiInfo)
            .forCodeGeneration(true)
            .genericModelSubstitutes(ResponseEntity.class)
            .select()
            .paths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
            .build();
        watch.stop();
        log.debug("Started Swagger in {} ms", watch.getTotalTimeMillis());
        return docket;
    }

    /**
     * Springfox configuration for the management endpoints (actuator) Swagger docs.
     *
     * @param appName               the application name
     * @param managementContextPath the path to access management endpoints
     * @param appVersion            the application version
     * @return the Swagger Springfox configuration
     */
    @Bean
    public Docket swaggerSpringfoxManagementDocket(@Value("${spring.application.name}") String appName,
                                                   @Value("${management.context-path}") String managementContextPath,
                                                   @Value("${info.project.version}") String appVersion) {
        String host = podiumProperties.getSwagger().getHost();
        String[] protocols = podiumProperties.getSwagger().getProtocols();
        return new Docket(DocumentationType.SWAGGER_2)
                .host(host)
                .protocols(new HashSet<>(Arrays.asList(protocols)))
                .apiInfo(new ApiInfo(appName + " management API", "Management endpoints documentation",
                        appVersion, "", ApiInfo.DEFAULT_CONTACT, "", "", new ArrayList<VendorExtension>()))
                .groupName("management")
                .forCodeGeneration(true)
                .directModelSubstitute(java.nio.ByteBuffer.class, String.class)
                .genericModelSubstitutes(ResponseEntity.class)
                .select()
                .paths(PathSelectors.regex(managementContextPath + ".*"))
                .build();
    }

    @Bean
    PageableParameterBuilderPlugin pageableParameterBuilderPlugin(TypeNameExtractor nameExtractor,
                                                                  TypeResolver resolver) {

        return new PageableParameterBuilderPlugin(nameExtractor, resolver);
    }

}
