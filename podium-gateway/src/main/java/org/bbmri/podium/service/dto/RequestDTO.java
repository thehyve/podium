package org.bbmri.podium.service.dto;


import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;

import org.bbmri.podium.common.domain.Organisation;
import org.bbmri.podium.domain.Attachment;
import org.bbmri.podium.domain.enumeration.RequestStatus;

/**
 * A DTO for the Request entity.
 */
public class RequestDTO implements Serializable {

    private Long id;

    @NotNull
    private RequestStatus status;

    private List<Organisation> organisations = new ArrayList<>();

    private Long parentRequestId;

    private Long requestDetailId;

    private Set<Attachment> attachments = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<Organisation> getOrganisations() {
        return organisations;
    }

    public void setOrganisations(List<Organisation> organisations) {
        this.organisations = organisations;
    }

    public Long getParentRequestId() {
        return parentRequestId;
    }

    public void setParentRequestId(Long requestId) {
        this.parentRequestId = requestId;
    }

    public Long getRequestDetailId() {
        return requestDetailId;
    }

    public void setRequestDetailId(Long requestDetailId) {
        this.requestDetailId = requestDetailId;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RequestDTO requestDTO = (RequestDTO) o;

        if ( ! Objects.equals(id, requestDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestDTO{" +
            "id=" + id +
            ", status='" + status + "'" +
            '}';
    }
}
