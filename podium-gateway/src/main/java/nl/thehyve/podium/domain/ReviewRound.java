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

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "review_round")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "review_round")
public class ReviewRound implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "review_round_seq_gen")
    @GenericGenerator(
        name = "review_round_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "review_round_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    @Column(name = "review_round_id")
    private Long id;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(unique = true, name = "request_detail")
    private RequestDetail requestDetail;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    @BatchSize(size = 1000)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @OrderColumn(name="review_feedback_order")
    @JoinTable(name = "review_round_review_feedback",
        joinColumns = @JoinColumn(name="review_round_id", referencedColumnName="review_round_id"),
        inverseJoinColumns = @JoinColumn(name="review_feedback_id", referencedColumnName="review_feedback_id"))
    private List<ReviewFeedback> reviewFeedback;

    @CreatedDate
    @Column(name = "start_date", nullable = false)
    private ZonedDateTime startDate = ZonedDateTime.now();

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "initiated_by")
    private UUID initiatedBy;

    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestDetail getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(RequestDetail requestDetail) {
        this.requestDetail = requestDetail;
    }

    public List<ReviewFeedback> getReviewFeedback() {
        return reviewFeedback;
    }

    public void setReviewFeedback(List<ReviewFeedback> reviewFeedback) {
        this.reviewFeedback = reviewFeedback;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public UUID getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(UUID initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReviewRound reviewRound = (ReviewRound) o;
        if (reviewRound.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, reviewRound.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ReviewRound{" +
            "id=" + id +
            ", requestDetail=" + requestDetail +
            ", reviewFeedback=" + reviewFeedback +
            ", startDate=" + startDate +
            ", endDate=" + endDate +
            ", initiatedBy=" + initiatedBy +
            '}';
    }
}
