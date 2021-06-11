/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "request_template")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class RequestTemplate extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_template_gen")
    @GenericGenerator(
        name = "request_template_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "request_template_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1000"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @Column(name="url", nullable = false)
    private String url;

    @Column(name="human_readable", nullable = false)
    private String humanReadable;

    @ElementCollection(targetClass = java.util.UUID.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name="request_template_organisations",
            joinColumns=@JoinColumn(name="request_template_id")
    )
    @Column(name = "organisation_uuid")
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    private Set<UUID> organisations = new HashSet<>();

    @Column(name="ntoken")
    private String nToken;

    /**
     * Only the database can return the UUID from the stored entity
     * Pre-persist will add a {@link UUID} to the entity
     * This setter is only added to satisfy mapstruct e.g.
     *
     * @param uuid is ignored.
     */
    public void setUuid(UUID uuid) {
        // pass
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestTemplate requestTemplate = (RequestTemplate) o;
        if (requestTemplate.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, requestTemplate.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestTemplate{" +
                "id=" + id +
                ", humanReadable='" + humanReadable + "'" +
                '}';
    }

}
