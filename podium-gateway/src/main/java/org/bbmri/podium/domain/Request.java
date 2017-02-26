package org.bbmri.podium.domain;

import org.bbmri.podium.common.domain.Organisation;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cache;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;

import org.bbmri.podium.domain.enumeration.RequestStatus;

/**
 * A Request.
 */
@Entity
@Table(name = "request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "request")
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_seq_gen")
    @GenericGenerator(
        name = "request_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status;

    @ElementCollection
    private List<UUID> organisations = new ArrayList<>();

    @ManyToOne
    private Request parentRequest;

    @OneToOne
    @JoinColumn(unique = true)
    private RequestDetail requestDetail;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "request_attachments",
               joinColumns = @JoinColumn(name="requests_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="attachments_id", referencedColumnName="id"))
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

    public Request status(RequestStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<UUID> getOrganisations() {
        return organisations;
    }

    public Request organisations(List<UUID> organisations) {
        this.organisations = organisations;
        return this;
    }

    public Request addOrganisations(UUID organisation) {
        this.organisations.add(organisation);
        return this;
    }

    public Request removeOrganisations(UUID organisation) {
        this.organisations.remove(organisation);
        return this;
    }

    public void setOrganisations(List<UUID> organisations) {
        this.organisations = organisations;
    }

    public Request getParentRequest() {
        return parentRequest;
    }

    public Request parentRequest(Request request) {
        this.parentRequest = request;
        return this;
    }

    public void setParentRequest(Request request) {
        this.parentRequest = request;
    }

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public Request requestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
        return this;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public Request attachments(Set<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public Request addAttachments(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public Request removeAttachments(Attachment attachment) {
        this.attachments.remove(attachment);
        return this;
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
        Request request = (Request) o;
        if (request.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, request.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Request{" +
            "id=" + id +
            ", status='" + status + "'" +
            '}';
    }
}
