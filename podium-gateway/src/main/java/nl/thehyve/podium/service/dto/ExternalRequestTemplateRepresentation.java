package nl.thehyve.podium.service.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * A DTO for the External Request Template entity.
 */
@Data
public class ExternalRequestTemplateRepresentation {
    Long id;

    UUID uuid;

    String url;
    String humanReadable;

    List<String> organizationIds;

    String nToken;
}
