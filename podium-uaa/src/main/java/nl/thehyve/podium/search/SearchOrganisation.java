/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.search;

import nl.thehyve.podium.domain.Organisation;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.Objects;

/**
 * An Organisation.
 */
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "organisation")
public class SearchOrganisation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private String uuid;

    private String name;

    private String shortName;

    private boolean activated;

    public void copyProperties(Organisation organisation) {
        this.name = organisation.getName();
        this.shortName = organisation.getShortName();
        this.activated = organisation.isActivated();
    }

    public SearchOrganisation() {

    }

    public SearchOrganisation(Organisation organisation) {
        this.id = organisation.getId();
        this.uuid = organisation.getUuid().toString();
        copyProperties(organisation);
    }

    public Long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public boolean isActivated() {
        return activated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchOrganisation organisation = (SearchOrganisation) o;
        if (organisation.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, organisation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Organisation{" +
            "id=" + id +
            ", uuid='" + uuid + "'" +
            ", name='" + name + "'" +
            ", shortName='" + shortName + "'" +
            '}';
    }

}
