/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * A PrincipalInvestigator.
 */
@Entity
@Table(name = "principal_investigator")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class PrincipalInvestigator implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "principal_investigator_seq_gen")
    @GenericGenerator(
        name = "principal_investigator_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "principal_investigator_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(name = "name", length = 150)
    private String name;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "affiliation", length = 150)
    private String affiliation;

    public String getName() {
        return name;
    }

    public PrincipalInvestigator name(String name) {
        this.name = name;
        return this;
    }

    public PrincipalInvestigator email(String email) {
        this.email = email;
        return this;
    }

    public PrincipalInvestigator jobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
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
        PrincipalInvestigator principalInvestigator = (PrincipalInvestigator) o;
        if (principalInvestigator.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, principalInvestigator.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PrincipalInvestigator{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", email='" + email + "'" +
            ", jobTitle='" + jobTitle + "'" +
            ", affiliation='" + affiliation + "'" +
            '}';
    }
}
