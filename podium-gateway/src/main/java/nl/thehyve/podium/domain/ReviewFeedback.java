/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "review_feedback",
    indexes = {
        @Index(name="review_feedback_reviewer_key", columnList = "reviewer")
    }
)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class ReviewFeedback implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_feedback_seq_gen")
    @GenericGenerator(
        name = "review_feedback_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "review_feedback_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    @Column(name = "review_feedback_id")
    private Long id;

    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @Column(name = "reviewer", nullable = false)
    private UUID reviewer;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "advice", nullable = false)
    private ReviewProcessOutcome advice = ReviewProcessOutcome.None;

    private ZonedDateTime date;

    private String summary;

    @Lob
    private String description;

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
        ReviewFeedback reviewFeedback = (ReviewFeedback) o;
        if (reviewFeedback.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, reviewFeedback.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ReviewFeedback{" +
            "id=" + id +
            ", reviewer=" + reviewer +
            ", advice=" + advice +
            ", date=" + date +
            '}';
    }
}
