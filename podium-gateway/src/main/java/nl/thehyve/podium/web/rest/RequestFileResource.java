/*
 * Copyright (c) 2017, 2018  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.enumeration.RequestFileType;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing files.
 */
@RestController
@RequestMapping("/api")
public class RequestFileResource {

    private final Logger log = LoggerFactory.getLogger(RequestFileResource.class);

    private static final String REQUEST_FILE_NAME = "requestFile";

    @Autowired
    private SecurityService securityService;

    @Autowired
    protected PodiumProperties podiumProperties;

    @Autowired
    protected RequestFileService requestFileService;

    @Autowired
    protected ExternalRequestTemplateService externalRequestTemplateService;


    /**
     * POST /requests/:uuid/files : Accept a RequestFile and add it to the request data
     * @return A confirmation of the upload
     */
    @PostMapping("/requests/{uuid}/files")
    @SecuredByAuthority({AuthorityConstants.ORGANISATION_ADMIN, AuthorityConstants.ORGANISATION_COORDINATOR,
                         AuthorityConstants.REVIEWER, AuthorityConstants.RESEARCHER})
    @Timed
    public ResponseEntity<RequestFileRepresentation> addFile(@RequestUuidParameter @PathVariable("uuid") UUID uuid,
                                          @RequestParam("file") MultipartFile file) throws ActionNotAllowed, IOException {
        AuthenticatedUser user = securityService.getCurrentUser();

        if (file.isEmpty()) {
            throw new InvalidRequest("Empty file.");
        }
        RequestFileRepresentation requestFileRepresentation = requestFileService.addFile(user, uuid, file, RequestFileType.NONE);
        return ResponseEntity.accepted().body(requestFileRepresentation);
    }

    /**
     * Return the resource for a file for a given request
     * @return the requested file
     */
    @GetMapping("/requests/{uuid}/files/{fileuuid}/download")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @SecuredByRequestOrganisationReviewer
    @Timed
    public ResponseEntity<InputStreamResource> downloadFile(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
                                          @PathVariable("fileuuid") UUID fileUuid) throws IOException{
        InputStreamResource resource = requestFileService.getFileResource(requestUuid, fileUuid);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(resource);
    }

    @GetMapping("/requests/{uuid}/files")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @SecuredByRequestOrganisationReviewer
    @Timed
    public ResponseEntity<List<RequestFileRepresentation>> listFiles(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid) {
        List<RequestFileRepresentation> files = requestFileService.getFilesForRequest(requestUuid);
        return ResponseEntity.ok(files);
    }

    @DeleteMapping("/requests/{uuid}/files/{fileuuid}")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<Void> deleteFile(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
            @PathVariable("fileuuid") UUID fileUuid) throws IOException{
        AuthenticatedUser user = securityService.getCurrentUser();
        requestFileService.deleteFile(user, requestUuid, fileUuid);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(REQUEST_FILE_NAME, fileUuid.toString())).build();
    }

    @PutMapping("/requests/{uuid}/files/{fileuuid}/type")
    @SecuredByRequestOwner
    @Timed
    public ResponseEntity<RequestFileRepresentation> setFileType(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
                                                                 @PathVariable("fileuuid") UUID fileUuid,
                                                                 @RequestBody RequestFileRepresentation requestFileRepresentation) {
        RequestFileType type = requestFileRepresentation.getRequestFileType();
        RequestFileRepresentation requestFile = requestFileService.setFileType(requestUuid, fileUuid, type);
        return ResponseEntity.ok(requestFile);
    }

}
