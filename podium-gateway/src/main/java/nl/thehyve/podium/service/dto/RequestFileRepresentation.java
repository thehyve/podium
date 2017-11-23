package nl.thehyve.podium.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;
import nl.thehyve.podium.enumeration.RequestFileType;

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

    private String owner;

    private Request request;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

    private Long fileSize;

    private RequestFileType requestFileType;

    private String fileName;

}
