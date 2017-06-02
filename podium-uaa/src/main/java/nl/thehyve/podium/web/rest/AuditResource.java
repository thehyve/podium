/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.web.rest;

import io.swagger.annotations.ApiParam;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.service.AuditEventService;
import nl.thehyve.podium.web.rest.util.PaginationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * REST controller for getting the audit events.
 */
@SecuredByAuthority({AuthorityConstants.PODIUM_ADMIN, AuthorityConstants.BBMRI_ADMIN})
@RestController
@RequestMapping("/management/audits")
public class AuditResource {

    private AuditEventService auditEventService;

    @Autowired
    public AuditResource(AuditEventService auditEventService) {
        this.auditEventService = auditEventService;
    }

    /**
     * GET  /audits : get a page of AuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping
    public ResponseEntity<List<AuditEvent>> getAll(@ApiParam Pageable pageable) throws URISyntaxException {
        Page<AuditEvent> page = auditEventService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /audits : get a page of AuditEvents between the fromDate and toDate.
     *
     * @param fromDate the start of the time period of AuditEvents to get
     * @param toDate the end of the time period of AuditEvents to get
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of AuditEvents in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @GetMapping(params = {"fromDate", "toDate"})
    public ResponseEntity<List<AuditEvent>> getByDates(
        @RequestParam(value = "fromDate") @DateTimeFormat(pattern="yyyy-MM-dd") Date fromDate,
        @RequestParam(value = "toDate") @DateTimeFormat(pattern="yyyy-MM-dd") Date toDate,
        @ApiParam Pageable pageable) throws URISyntaxException {
        Page<AuditEvent> page = auditEventService.findByDates(fromDate, toDate, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/management/audits");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /audits/:id : get an AuditEvent by id.
     *
     * @param id the id of the entity to get
     * @return the ResponseEntity with status 200 (OK) and the AuditEvent in body, or status 404 (Not Found)
     */
    @GetMapping("/{id:.+}")
    public ResponseEntity<AuditEvent> get(@PathVariable Long id) {
        return auditEventService.find(id)
                .map((entity) -> new ResponseEntity<>(entity, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
