package nl.thehyve.podium.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.IdentifiableRequest;
import nl.thehyve.podium.common.service.dto.UserRepresentation;
import nl.thehyve.podium.domain.Request;

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

    private Request request;

    private ZonedDateTime createdDate;

    private ZonedDateTime lastModifiedDate;

}
