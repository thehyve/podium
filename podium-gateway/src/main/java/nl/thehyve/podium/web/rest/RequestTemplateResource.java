/*
 * Copyright (c) 2017, 2018  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.dto.ExternalRequestRepresentation;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.service.dto.ExternalRequestTemplateRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing requests templates.
 */
@RestController
@RequestMapping("/api")
public class RequestTemplateResource {

    private final Logger log = LoggerFactory.getLogger(RequestTemplateResource.class);

    @Autowired
    protected PodiumProperties podiumProperties;

    @Autowired
    protected RequestFileService requestFileService;

    @Autowired
    protected ExternalRequestTemplateService externalRequestTemplateService;


    /**
     * Accept external request data and create a new request template. This endpoint doesn't require a Podium login, but
     * does check the Authorization header. It assumes the sender is including a Basic value that is treated like an
     * API key. The values are stored in the config file and only stored values are allowed. It is done this way to
     * connect to other old services that cannot use OAuth2, like Molgenis.
     *
     * See the documentation of Molgenis on how this endpoint should behave:
     * <a href="https://molgenis.gitbooks.io/molgenis/user_documentation/biobank-directory.html">Molgenis &ndash; Biobank Directory</a>.
     *
     * @return String URL to request form with filled in data
     */
    @PostMapping("/public/requests/templates")
    @Public
    @Timed
    public ResponseEntity<URI> createDraftByExternalRequest(
        @RequestBody ExternalRequestRepresentation externalRequestRepresentation,
        @RequestHeader("Authorization") String authorization)
        throws URISyntaxException {

        if(! authorization.substring(0, 6).equals("Basic ")){
            throw new AccessDenied("No Auth provided");
        }
        // Turn base64 string into normal string with a username:password format
        String base64 = authorization.substring(6, authorization.length());
        String id = new String(Base64.decode(base64.getBytes()), Charset.forName("UTF-8"));

        List<String> allowedIds = podiumProperties.getAccess().getExternalRequest();

        if (!allowedIds.contains(id)){
            throw new AccessDenied(String.format("Provided id: %s is not on the allowed id list for creating" +
                " external requests", id));
        }

        ExternalRequestTemplateRepresentation externalRequestTemplateRepresentation =
            externalRequestTemplateService.createTemplate(externalRequestRepresentation);

        String callbackURL = String.format("%s/#/requests/new?template_uuid=%s",
            podiumProperties.getMail().getBaseUrl(), externalRequestTemplateRepresentation.getUuid()
        );

        log.debug("Returning URL {}", callbackURL);
        return new ResponseEntity<>(new URI(callbackURL), HttpStatus.ACCEPTED);
    }

    /**
     * GET /requests/external/:uuid : Accept external request data and create a new request draft
     * @return redirect to request form with filled in data
     */
    @GetMapping("/requests/templates/{uuid}")
    @SecuredByAuthority({AuthorityConstants.RESEARCHER})
    @Timed
    public ResponseEntity<ExternalRequestTemplateRepresentation> getRequestTemplate(@PathVariable("uuid") UUID uuid){
        ExternalRequestTemplateRepresentation externalRequestTemplateRepresentation =
            externalRequestTemplateService.getTemplate(uuid);
        return ResponseEntity.ok(externalRequestTemplateRepresentation);
    }

}
