/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.thehyve.podium.common.enumeration.RequestType;
import nl.thehyve.podium.common.validation.Required;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A RequestDetailRepresentation.
 */
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
    @Size(max = 500)
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackground() {
        return background;
    }

    public RequestDetailRepresentation background(String background) {
        this.background = background;
        return this;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getResearchQuestion() {
        return researchQuestion;
    }

    public RequestDetailRepresentation researchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
        return this;
    }

    public void setResearchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public RequestDetailRepresentation hypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
        return this;
    }

    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    public String getMethods() {
        return methods;
    }

    public RequestDetailRepresentation methods(String methods) {
        this.methods = methods;
        return this;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public String getRelatedRequestNumber() {
        return relatedRequestNumber;
    }

    public RequestDetailRepresentation relatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
        return this;
    }

    public void setRelatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
    }

    public PrincipalInvestigatorRepresentation getPrincipalInvestigator() {
        return principalInvestigator;
    }

    public void setPrincipalInvestigator(PrincipalInvestigatorRepresentation principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    public Boolean getCombinedRequest() {
        return combinedRequest;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public RequestDetailRepresentation searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    @JsonIgnore
    public String getRequestTypesString() {
        return String.join(", ", requestType.stream().map(RequestType::name).collect(Collectors.toList()));
    }

    public Set<RequestType> getRequestType() { return requestType; }

    public void setRequestType(Set<RequestType> requestType) { this.requestType = requestType; }

    public Boolean isCombinedRequest() {
        return combinedRequest;
    }

    public RequestDetailRepresentation combinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
        return this;
    }

    public void setCombinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestDetailRepresentation requestDetail = (RequestDetailRepresentation) o;
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
        return "RequestDetailRepresentation{" +
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
