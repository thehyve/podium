/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.search;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.io.Serializable;
import java.util.Objects;

/**
 * An ES search user.
 */
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "searchuser")
public class SearchUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String uuid;

    private String login;

    private String firstName;

    private String lastName;

    private String email;

    private String telephone;

    private String institute;

    private String department;

    private String jobTitle;

    private String specialism;

    @CompletionField(maxInputLength = 60)
    private Completion fullNameSuggest;

    private String fullName;

    public SearchUser() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getInstitute() {
        return institute;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getSpecialism() {
        return specialism;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public Completion getFullNameSuggest() { return fullNameSuggest; }

    public void setFullNameSuggest(Completion fullNameSuggest) { this.fullNameSuggest = fullNameSuggest; }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchUser searchUser = (SearchUser) o;
        if (searchUser.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, searchUser.id);
    }

    @Override
    public String toString() {
        return "SearchUser{" +
            "id=" + id +
            ", uuid='" + uuid + '\'' +
            ", login='" + login + '\'' +
            ", fullNameSuggest='" + fullNameSuggest + '\'' +
            ", fullName='" + fullName + '\'' +
            ", email='" + email + '\'' +
            ", telephone='" + telephone + '\'' +
            ", institute='" + institute + '\'' +
            ", department='" + department + '\'' +
            ", jobTitle='" + jobTitle + '\'' +
            ", specialism='" + specialism + '\'' +
            '}';
    }
}
