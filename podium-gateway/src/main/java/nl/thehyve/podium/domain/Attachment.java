/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.Data;
import nl.thehyve.podium.common.enumeration.AttachmentType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * A Attachment.
 */
@Entity
@Table(name = "attachment")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "attachment")
@Data
public class Attachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachment_seq_gen")
    @GenericGenerator(
        name = "attachment_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "attachment_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "filename", nullable = false)
    private String filename;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AttachmentType type;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "mime_type")
    private String mimeType;

    @NotNull
    @Column(name = "uploader")
    private UUID uploader;

    @ManyToOne
    private Request request;

    public Attachment name(String name) {
        this.name = name;
        return this;
    }

    public Attachment description(String description) {
        this.description = description;
        return this;
    }

    public Attachment filename(String filename) {
        this.filename = filename;
        return this;
    }

    public Attachment type(AttachmentType type) {
        this.type = type;
        return this;
    }

    public Attachment date(LocalDate date) {
        this.date = date;
        return this;
    }

    public Attachment mimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public Attachment uploader(UUID user) {
        this.uploader = user;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Attachment attachment = (Attachment) o;
        if (attachment.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, attachment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Attachment{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", description='" + description + "'" +
            ", filename='" + filename + "'" +
            ", type='" + type + "'" +
            ", date='" + date + "'" +
            ", mimeType='" + mimeType + "'" +
            '}';
    }
}
