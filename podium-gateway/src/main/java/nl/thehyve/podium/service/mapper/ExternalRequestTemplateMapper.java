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
