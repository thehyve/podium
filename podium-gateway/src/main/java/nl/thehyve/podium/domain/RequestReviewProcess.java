/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import nl.thehyve.podium.common.enumeration.DecisionOutcome;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Parameter;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "request_review_process")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "requestreviewprocess")
public class RequestReviewProcess extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_seq_gen")
    @GenericGenerator(
        name = "request_seq_gen",
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
    private DecisionOutcome decision = DecisionOutcome.None;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public RequestReviewStatus getStatus() {
        return status;
    }

    public void setStatus(RequestReviewStatus status) {
        this.status = status;
    }

    public DecisionOutcome getDecision() {
        return decision;
    }

    public void setDecision(DecisionOutcome decision) {
        this.decision = decision;
    }

}
