/*
 * Copyright (c) 2017, 2018  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import nl.thehyve.podium.common.config.PodiumProperties;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.common.exceptions.InvalidRequest;
import nl.thehyve.podium.common.exceptions.ResourceNotFound;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.security.annotations.*;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.enumeration.RequestFileType;
import nl.thehyve.podium.service.*;
import nl.thehyve.podium.common.service.dto.RequestFileRepresentation;
import nl.thehyve.podium.common.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
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
    protected RequestTemplateService requestTemplateService;


    /**
     * POST /requests/:uuid/files : Accept a RequestFile and add it to the request.
     * @param uuid the uuid of the request to add the file to.
     * @param file the uploaded file.
     * @return A confirmation of the upload
     */
    @PostMapping("/requests/{uuid}/files")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    public ResponseEntity<RequestFileRepresentation> addFile(@RequestUuidParameter @PathVariable("uuid") UUID uuid,
                                          @RequestParam("file") MultipartFile file) throws ActionNotAllowed, IOException {
        AuthenticatedUser user = securityService.getCurrentUser();
        log.debug("REST request to upload a file for request : {} ", uuid);
        if (file.isEmpty()) {
            log.debug("Empty file uploaded for request : {} ", uuid);
            throw new InvalidRequest("Empty file.");
        }
        RequestFileRepresentation requestFileRepresentation = requestFileService.addFile(user, uuid, file, RequestFileType.NONE);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(requestFileRepresentation);
    }

    /**
     * GET /requests/:uuid/files/:fileuuid/download : Return the resource for a file for a given request.
     *
     * @param requestUuid the uuid of the request.
     * @param fileUuid the uuid of the file.
     * @return the requested file
     */
    @GetMapping("/requests/{uuid}/files/{fileuuid}/download")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @SecuredByRequestOrganisationReviewer
    public ResponseEntity<InputStreamResource> downloadFile(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
                                          @PathVariable("fileuuid") UUID fileUuid) throws IOException{
        log.debug("REST request to download file {} for request {} ", fileUuid, requestUuid);
        InputStreamResource resource = requestFileService.getFileResource(requestUuid, fileUuid);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * GET /requests/:uuid/files : Return the list of files for a given request.
     *
     * @param requestUuid the uuid of the request.
     * @return the list of file representations.
     */
    @GetMapping("/requests/{uuid}/files")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    @SecuredByRequestOrganisationReviewer
    public ResponseEntity<List<RequestFileRepresentation>> listFiles(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid) {
        log.debug("REST request to retrieve the list of files for request {} ", requestUuid);
        //Get list sorted by created date so the order shown on a request page is always the same.
        List<RequestFileRepresentation> files = requestFileService.getFilesForRequest(requestUuid);
        return ResponseEntity.ok(files);
    }

    /**
     * DELETE /requests/:uuid/files/:fileuuid : Delete a file from a request.
     *
     * @param requestUuid the uuid of the request.
     * @param fileUuid the uuid of the file.
     * @throws ResourceNotFound iff the request or the file cannot be found or if the user is not the owner.
     * @throws IOException iff an error occurs while deleting the file from the filesystem.
     */
    @DeleteMapping("/requests/{uuid}/files/{fileuuid}")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    public ResponseEntity<Void> deleteFile(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
            @PathVariable("fileuuid") UUID fileUuid) throws IOException, ResourceNotFound {
        log.debug("REST request to delete file {} for request {} ", fileUuid, requestUuid);
        AuthenticatedUser user = securityService.getCurrentUser();
        requestFileService.deleteFile(user, requestUuid, fileUuid);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(REQUEST_FILE_NAME, fileUuid.toString())).build();
    }

    /**
     * PUT /requests/:uuid/files/:fileuuid/type : Update the file type for a request file.
     *
     * @param requestUuid the uuid of the request.
     * @param fileUuid the uuid of the file.
     * @param requestFileRepresentation a request file object with the new value for the request file type.
     * @return a representation of the updated request.
     */
    @PutMapping("/requests/{uuid}/files/{fileuuid}/type")
    @SecuredByRequestOwner
    @SecuredByRequestOrganisationCoordinator
    public ResponseEntity<RequestFileRepresentation> setFileType(@RequestUuidParameter @PathVariable("uuid") UUID requestUuid,
                                                                 @PathVariable("fileuuid") UUID fileUuid,
                                                                 @RequestBody RequestFileRepresentation requestFileRepresentation) {
        RequestFileType type = requestFileRepresentation.getRequestFileType();
        log.debug("REST request to set the file type of file {} for request {} to : {}", fileUuid, requestUuid, type);
        AuthenticatedUser user = securityService.getCurrentUser();
        RequestFileRepresentation requestFile = requestFileService.setFileType(user, requestUuid, fileUuid, type);
        return ResponseEntity.ok(requestFile);
    }

}
