/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import nl.thehyve.podium.domain.Attachment;
import nl.thehyve.podium.repository.AttachmentRepository;
import nl.thehyve.podium.repository.search.AttachmentSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Service Implementation for managing Attachment.
 */
@Service
@Transactional
public class AttachmentService {

    private final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;

    private final AttachmentSearchRepository attachmentSearchRepository;

    public AttachmentService(AttachmentRepository attachmentRepository, AttachmentSearchRepository attachmentSearchRepository) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentSearchRepository = attachmentSearchRepository;
    }

    /**
     * Save a attachment.
     *
     * @param attachment the entity to save
     * @return the persisted entity
     */
    public Attachment save(Attachment attachment) {
        log.debug("Request to save Attachment : {}", attachment);
        Attachment result = attachmentRepository.save(attachment);
        attachmentSearchRepository.save(result);
        return result;
    }

    /**
     *  Get all the attachments.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Attachment> findAll(Pageable pageable) {
        log.debug("Request to get all Attachments");
        Page<Attachment> result = attachmentRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one attachment by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Attachment findOne(Long id) {
        log.debug("Request to get Attachment : {}", id);
        Attachment attachment = attachmentRepository.findOne(id);
        return attachment;
    }

    /**
     *  Delete the  attachment by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Attachment : {}", id);
        attachmentRepository.delete(id);
        attachmentSearchRepository.delete(id);
    }

    /**
     * Search for the attachment corresponding to the query.
     *
     *  @param query the query of the search
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Attachment> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of Attachments for query {}", query);
        Page<Attachment> result = attachmentSearchRepository.search(queryStringQuery(query), pageable);
        return result;
    }
}
