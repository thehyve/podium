package nl.thehyve.podium.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.service.dto.RequestRepresentation;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.enumeration.RequestFileType;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * A DTO for the Request File entity.
 */
@Data
public class RequestFileRepresentation implements Serializable {
    private UUID uuid;

    private UserRepresentation owner;

    private RequestRepresentation request;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private Long fileSize;

    private RequestFileType requestFileType;

    private String fileName;
}
