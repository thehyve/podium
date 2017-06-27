/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.Data;
import nl.thehyve.podium.common.enumeration.RequestType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * A RequestDetail.
 */
@Entity
@Table(name = "request_detail")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "requestdetail")
@Data
public class RequestDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_detail_seq_gen")
    @GenericGenerator(
        name = "request_detail_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_detail_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "background", length = 2000)
    private String background;

    @Column(name = "research_question", length = 300)
    private String researchQuestion;

    @Column(name = "hypothesis", length = 5000)
    private String hypothesis;

    @Column(name = "methods", length = 10000)
    private String methods;

    @Column(name = "related_request_number", length = 50)
    private String relatedRequestNumber;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "principal_investigator")
    private PrincipalInvestigator principalInvestigator;

    @Column(name = "search_query", length = 500)
    private String searchQuery;

    @ElementCollection(targetClass = RequestType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name="request_detail_request_types",
        joinColumns=@JoinColumn(name="request_detail_id")
    )
    private Set<RequestType> requestType;

    @Column(name = "combined_request")
    private Boolean combinedRequest;

    public RequestDetail title(String title) {
        this.title = title;
        return this;
    }

    public RequestDetail background(String background) {
        this.background = background;
        return this;
    }

    public RequestDetail researchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
        return this;
    }

    public RequestDetail hypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
        return this;
    }

    public RequestDetail methods(String methods) {
        this.methods = methods;
        return this;
    }

    public RequestDetail relatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
        return this;
    }

    public RequestDetail searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    public RequestDetail combinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
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
        RequestDetail requestDetail = (RequestDetail) o;
        if (requestDetail.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, requestDetail.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestDetail{" +
            "id=" + id +
            ", title='" + title + "'" +
            ", background='" + background + "'" +
            ", researchQuestion='" + researchQuestion + "'" +
            ", hypothesis='" + hypothesis + "'" +
            ", methods='" + methods + "'" +
            ", relatedRequestNumber='" + relatedRequestNumber + "'" +
            ", searchQuery='" + searchQuery + "'" +
            ", requestType='" + requestType + "'" +
            ", combinedRequest='" + combinedRequest + "'" +
            '}';
    }
}
