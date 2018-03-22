/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service;

import com.codahale.metrics.annotation.Timed;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.ExternalRequestRepresentation;
import nl.thehyve.podium.domain.ExternalRequestTemplate;
import nl.thehyve.podium.repository.ExternalRequestTemplateRepository;
import nl.thehyve.podium.service.dto.ExternalRequestTemplateRepresentation;
import nl.thehyve.podium.service.mapper.ExternalRequestTemplateMapper;
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
public class ExternalRequestTemplateService {

    @Autowired
    protected ExternalRequestTemplateRepository externalRequestTemplateRepository;

    @Autowired
    protected ExternalRequestTemplateMapper externalRequestTemplateMapper;

    public ExternalRequestTemplateRepresentation createTemplate(
        ExternalRequestRepresentation externalRequestRepresentation
    ){
        ExternalRequestTemplate externalRequestTemplate = new ExternalRequestTemplate();
        externalRequestTemplate.setNToken(externalRequestRepresentation.getNToken());
        externalRequestTemplate.setHumanReadable(externalRequestRepresentation.getHumanReadable());
        externalRequestTemplate.setUrl(externalRequestRepresentation.getUrl());

        List<Map<String, String>> collections = externalRequestRepresentation.getCollections();
        // Get the String id's from the external request and turn them into a list of relevant organisations
        List<String> organisationUUIDs = new ArrayList<>();

        for (Map<String, String> collection : collections) {
            String biobankId = collection.get("biobankID");

            try {
                UUID biobankUUID = UUID.fromString(biobankId);
                organisationUUIDs.add(biobankUUID.toString());
            } catch (IllegalArgumentException e) {
                continue;
            }

        }

        String organisationIds = String.join(",", organisationUUIDs);
        externalRequestTemplate.setOrganizationIds(organisationIds);

        externalRequestTemplateRepository.save(externalRequestTemplate);
        return externalRequestTemplateMapper.processingExternalRequestTemplateToExternalRequestTemplateDto(
            externalRequestTemplate);
    }

    public ExternalRequestTemplateRepresentation getTemplate(UUID uuid){
        ExternalRequestTemplate externalRequestTemplate = externalRequestTemplateRepository.findOneByUuid(uuid);

        return externalRequestTemplateMapper.processingExternalRequestTemplateToExternalRequestTemplateDto(externalRequestTemplate);
    }

}
