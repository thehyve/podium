/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.domain.ExternalRequestTemplate;
import nl.thehyve.podium.service.dto.ExternalRequestTemplateRepresentation;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Arrays;

@Mapper(componentModel = "spring")
public class ExternalRequestTemplateMapper {

    public ExternalRequestTemplateRepresentation processingExternalRequestTemplateToExternalRequestTemplateDto(
        ExternalRequestTemplate externalRequestTemplate
    ){
        ExternalRequestTemplateRepresentation externalRequestTemplateRepresentation =
            new ExternalRequestTemplateRepresentation();

        externalRequestTemplateRepresentation.setNToken(externalRequestTemplate.getNToken());
        externalRequestTemplateRepresentation.setHumanReadable(externalRequestTemplate.getHumanReadable());
        externalRequestTemplateRepresentation.setUrl(externalRequestTemplate.getUrl());
        externalRequestTemplateRepresentation.setId(externalRequestTemplate.getId());
        externalRequestTemplateRepresentation.setUuid(externalRequestTemplate.getUuid());

        List<String> organizationIds = Arrays.asList(externalRequestTemplate.getOrganizationIds().split(","));

        externalRequestTemplateRepresentation.setOrganizationIds(organizationIds);

        return externalRequestTemplateRepresentation;
    }
}
