/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */
package nl.thehyve.podium.service.representation;

import nl.thehyve.podium.domain.enumeration.RequestType;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * A RequestDetailRepresentation.
 */
public class RequestDetailRepresentation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String background;

    private String researchQuestion;

    private String hypothesis;

    private String methods;

    private String relatedRequestNumber;

    private PrincipalInvestigatorRepresentation principalInvestigator;

    private String searchQuery;

    private Set<RequestType> requestType;

    private Boolean combinedRequest;

//    private Request request;

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

//    public Request getRequest() {
//        return request;
//    }
//
//    public RequestDetailRepresentation request(Request request) {
//        this.request = request;
//        return this;
//    }
//
//    public void setRequest(Request request) {
//        this.request = request;
//    }

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
