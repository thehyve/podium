/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.Data;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.common.enumeration.ReviewProcessOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "request_review_process",
    indexes = {
        @Index(name = "request_review_process_status_key", columnList = "status"),
        @Index(name = "request_review_process_decision_key", columnList = "status,decision"),
    }
)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class RequestReviewProcess extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_review_gen")
    @GenericGenerator(
        name = "request_review_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_review_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(name = "process_instance_id", nullable = false)
    private String processInstanceId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestReviewStatus status = RequestReviewStatus.None;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private ReviewProcessOutcome decision = ReviewProcessOutcome.None;

}
