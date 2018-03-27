/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.mapper;

import nl.thehyve.podium.common.service.dto.RequestTemplateRepresentation;
import nl.thehyve.podium.domain.RequestTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

import java.util.*;

@Mapper(componentModel = "spring")
public abstract class RequestTemplateMapper {

    /**
     * The key used for extracting organisation uuids from {@link RequestTemplateRepresentation#collections}.
     */
    public static String ORGANISATION_UUID_KEY = "biobankID";

    public abstract RequestTemplateRepresentation requestTemplateToRequestTemplateDto(RequestTemplate requestTemplate);

    /**
     * Copy properties of the request template representation to the entity, except organisation ids.
     * @param requestTemplateRepresentation data to be mapped
     * @param requestTemplate entity to map to
     * @return the mapping target
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "uuid", ignore = true),
            @Mapping(target = "organisations", ignore = true)
    })
    abstract RequestTemplate minimalRequestTemplateDtoToRequestTemplate(
            RequestTemplateRepresentation requestTemplateRepresentation,
            @MappingTarget RequestTemplate requestTemplate);

    /**
     * Copy properties of the request template representation to the entity.
     * The organisation uuids are copied from the collections field,
     * from the property with key {@link #ORGANISATION_UUID_KEY}.
     *
     * @param requestTemplateRepresentation data to be mapped
     * @param requestTemplate entity to map to
     * @return the mapping target
     */
    public RequestTemplate processingRequestTemplateDtoToRequestTemplate(
            RequestTemplateRepresentation requestTemplateRepresentation,
            @MappingTarget RequestTemplate requestTemplate) {
        requestTemplate = minimalRequestTemplateDtoToRequestTemplate(requestTemplateRepresentation, requestTemplate);

        List<Map<String, String>> collections = requestTemplateRepresentation.getCollections();
        // Get the uuids of organisations from the collections field.
        Set<UUID> organisationUUIDs = new HashSet<>();
        if (collections != null) {
            for (Map<String, String> collection : collections) {
                String biobankId = collection.get(ORGANISATION_UUID_KEY);
                if (biobankId != null) {
                    try {
                        UUID biobankUUID = UUID.fromString(biobankId);
                        organisationUUIDs.add(biobankUUID);
                    } catch (IllegalArgumentException e) {
                        // skip
                    }
                }
            }
        }
        requestTemplate.setOrganisations(organisationUUIDs);
        return requestTemplate;
    }

}
