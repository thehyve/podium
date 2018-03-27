/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.web.rest;

/**
 * Created by bernd on 25/03/2017.
 */

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.security.AuthorityConstants;
import nl.thehyve.podium.common.security.annotations.SecuredByAuthority;
import nl.thehyve.podium.common.service.SecurityService;
import nl.thehyve.podium.common.web.rest.util.HeaderUtil;
import nl.thehyve.podium.service.ElasticsearchIndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;

/**
 * REST controller for managing Elasticsearch index.
 */
@RestController
@RequestMapping("/api")
public class ElasticsearchIndexResource {

    private final Logger log = LoggerFactory.getLogger(ElasticsearchIndexResource.class);

    @Autowired
    private ElasticsearchIndexService elasticsearchIndexService;

    public ElasticsearchIndexResource( ) {}

    /**
     * Reindex all Elasticsearch documents
     *
     * @throws URISyntaxException Exception thrown when URI is malformed.
     * @return ResponseEntity without a type
     */
    @SecuredByAuthority({ AuthorityConstants.PODIUM_ADMIN })
    @GetMapping(value = "/elasticsearch/index",
        produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    public ResponseEntity<Void> reindexAll() throws URISyntaxException {
        log.info("REST request to reindex Elasticsearch by user : {}", SecurityService.getCurrentUserLogin());
        elasticsearchIndexService.reindexAll();
        return ResponseEntity.accepted()
            .headers(HeaderUtil.createAlert("elasticsearch.reindex.accepted", null))
            .build();
    }
}
