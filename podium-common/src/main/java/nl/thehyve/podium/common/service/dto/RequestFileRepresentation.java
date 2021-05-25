package nl.thehyve.podium.common.service.dto;

import lombok.Data;
import nl.thehyve.podium.common.enumeration.RequestFileType;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A DTO for the Request File entity.
 */
@Data
public class RequestFileRepresentation implements Serializable {

    private UUID uuid;

    private UserRepresentation owner;

    private RequestRepresentation request;

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;

    private Long fileByteSize;

    private RequestFileType requestFileType;

    private String fileName;

    private UserRepresentation uploader;

}
