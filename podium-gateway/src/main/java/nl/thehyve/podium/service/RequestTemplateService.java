/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.service.dto.RequestTemplateRepresentation;
import nl.thehyve.podium.domain.RequestTemplate;
import nl.thehyve.podium.repository.RequestTemplateRepository;
import nl.thehyve.podium.service.mapper.RequestTemplateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service Implementation for managing external request templates.
 */
@Service
@Transactional
@Timed
public class RequestTemplateService {

    @Autowired
    protected RequestTemplateRepository requestTemplateRepository;

    @Autowired
    protected RequestTemplateMapper requestTemplateMapper;


    /**
     * Save a new request template
     * @param requestTemplateRepresentation the template data to base the request template on.
     * @return a representation of the saved request template.
     */
    public RequestTemplateRepresentation createTemplate(RequestTemplateRepresentation requestTemplateRepresentation){
        RequestTemplate requestTemplate = new RequestTemplate();
        requestTemplate = requestTemplateMapper.processingRequestTemplateDtoToRequestTemplate(
                requestTemplateRepresentation, requestTemplate);
        requestTemplate = requestTemplateRepository.save(requestTemplate);
        return requestTemplateMapper.requestTemplateToRequestTemplateDto(requestTemplate);
    }

    /**
     * Retrieve a request template by its uuid.
     * @param uuid the uuid of the request template.
     * @return a representation of the request template.
     */
    @Transactional(readOnly = true)
    public RequestTemplateRepresentation getTemplate(UUID uuid) {
        RequestTemplate requestTemplate = requestTemplateRepository.findOneByUuid(uuid);
        return requestTemplateMapper.requestTemplateToRequestTemplateDto(requestTemplate);
    }

}
