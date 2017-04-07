/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.security.UserAuthenticationToken;
import nl.thehyve.podium.common.service.dto.OrganisationDTO;
import nl.thehyve.podium.security.SecurityUtils;
import nl.thehyve.podium.service.OrganisationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/proxy")
public class OrganisationProxy {

    private final Logger log = LoggerFactory.getLogger(OrganisationProxy.class);

    @Autowired
    OrganisationService organisationService;

    void handleException(Throwable e) {
        if (e instanceof FeignException) {
            if (((FeignException)e).status() == HttpStatus.FORBIDDEN.value()) {
                throw new AccessDenied("Access denied", e);
            }
        } else {
            if (e.getCause() != null) {
                handleException(e.getCause());
            }
        }
    }

    /**
     * Fetch organisations.
     *
     * @return a list of organisations.
     */
    @GetMapping("/organisations")
    @Timed
    public ResponseEntity<List<OrganisationDTO>> getOrganisations() throws URISyntaxException {
        UserAuthenticationToken user = SecurityUtils.getCurrentUser();
        log.info("Fetching organisations for {}.", user);

        try {
            List<OrganisationDTO> organisations = organisationService.findAllOrganisations();
            return ResponseEntity.ok(organisations);
        } catch (FeignException|HystrixRuntimeException e) {
            handleException(e);
            throw e;
        }
    }
}
