/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.validation.Required;

import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A RequestDetailRepresentation.
 */
@Data
public class RequestDetailRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Required
    @Size(max = 50)
    private String title;

    @Required
    @Size(max = 2000)
    private String background;

    @Required
    @Size(max = 300)
    private String researchQuestion;

    @Required
    @Size(max = 5000)
    private String hypothesis;

    @Required
    @Size(max = 10000)
    private String methods;

    @Size(max = 50)
    private String relatedRequestNumber;

    @Valid
    private PrincipalInvestigatorRepresentation principalInvestigator;

    @Required
    @Column(columnDefinition="TEXT")
    private String searchQuery;

    private Set<RequestType> requestType;

    private Boolean combinedRequest;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public RequestDetailRepresentation title(String title) {
        this.title = title;
        return this;
    }

    public RequestDetailRepresentation background(String background) {
        this.background = background;
        return this;
    }

    public RequestDetailRepresentation researchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
        return this;
    }

    public RequestDetailRepresentation hypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
        return this;
    }

    public RequestDetailRepresentation methods(String methods) {
        this.methods = methods;
        return this;
    }

    public RequestDetailRepresentation relatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
        return this;
    }

    public void setRelatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
    }

    public RequestDetailRepresentation searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    @JsonIgnore
    public String getRequestTypesString() {
        return String.join(", ", requestType.stream().map(RequestType::name).collect(Collectors.toList()));
    }

    public RequestDetailRepresentation combinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
        return this;
    }
}
