package org.bbmri.podium.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Parameter;
import javax.persistence.Table;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

import org.bbmri.podium.domain.enumeration.RequestType;

/**
 * A RequestDetail.
 */
@Entity
@Table(name = "request_detail")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "requestdetail")
public class RequestDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_detail_seq_gen")
    @GenericGenerator(
        name = "request_detail_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "request_detail_seq"),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @NotNull
    @Size(min = 1, max = 2000)
    @Column(name = "background", length = 2000, nullable = false)
    private String background;

    @NotNull
    @Size(min = 1, max = 300)
    @Column(name = "research_question", length = 300, nullable = false)
    private String researchQuestion;

    @NotNull
    @Size(min = 1, max = 5000)
    @Column(name = "hypothesis", length = 5000, nullable = false)
    private String hypothesis;

    @NotNull
    @Size(min = 1, max = 10000)
    @Column(name = "methods", length = 10000, nullable = false)
    private String methods;

    @Size(max = 50)
    @Column(name = "related_request_number", length = 50)
    private String relatedRequestNumber;

    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "search_query", length = 500, nullable = false)
    private String searchQuery;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false)
    private RequestType requestType;

    @Column(name = "combined_request")
    private Boolean combinedRequest;

    @OneToOne(mappedBy = "requestDetail")
    @JsonIgnore
    private Request request;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public RequestDetail title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackground() {
        return background;
    }

    public RequestDetail background(String background) {
        this.background = background;
        return this;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getResearchQuestion() {
        return researchQuestion;
    }

    public RequestDetail researchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
        return this;
    }

    public void setResearchQuestion(String researchQuestion) {
        this.researchQuestion = researchQuestion;
    }

    public String getHypothesis() {
        return hypothesis;
    }

    public RequestDetail hypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
        return this;
    }

    public void setHypothesis(String hypothesis) {
        this.hypothesis = hypothesis;
    }

    public String getMethods() {
        return methods;
    }

    public RequestDetail methods(String methods) {
        this.methods = methods;
        return this;
    }

    public void setMethods(String methods) {
        this.methods = methods;
    }

    public String getRelatedRequestNumber() {
        return relatedRequestNumber;
    }

    public RequestDetail relatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
        return this;
    }

    public void setRelatedRequestNumber(String relatedRequestNumber) {
        this.relatedRequestNumber = relatedRequestNumber;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public RequestDetail searchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RequestDetail requestType(RequestType requestType) {
        this.requestType = requestType;
        return this;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public Boolean isCombinedRequest() {
        return combinedRequest;
    }

    public RequestDetail combinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
        return this;
    }

    public void setCombinedRequest(Boolean combinedRequest) {
        this.combinedRequest = combinedRequest;
    }

    public Request getRequest() {
        return request;
    }

    public RequestDetail request(Request request) {
        this.request = request;
        return this;
    }

    public void setRequest(Request request) {
        this.request = request;
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
