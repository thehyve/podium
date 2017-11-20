package nl.thehyve.podium.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.service.dto.UserRepresentation;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * A DTO for the Request File entity.
 */
@Data
public class RequestFileRepresentation implements Serializable {
    private Long id;

    private UUID uuid;

    private UUID owner;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private String fileDriveLocation;
}
