/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;


import nl.thehyve.podium.common.validation.Required;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the PrincipalInvestigator entity.
 */
public class PrincipalInvestigatorRepresentation implements Serializable {

    private Long id;

    @Required
    @Size(max = 150)
    private String name;

    @Email
    @Required
    @Size(max = 150)
    private String email;

    @Required
    @Size(max = 150)
    private String jobTitle;

    @Required
    @Size(max = 150)
    private String affiliation;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getJobTitle() {
        return jobTitle;
    }
    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getAffiliation() {
        return affiliation;
    }
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PrincipalInvestigatorRepresentation principalInvestigatorDTO = (PrincipalInvestigatorRepresentation) o;

        if ( ! Objects.equals(id, principalInvestigatorDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PrincipalInvestigatorDTO{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", email='" + email + "'" +
            ", jobTitle='" + jobTitle + "'" +
            ", affiliation='" + affiliation + "'" +
            '}';
    }
}
