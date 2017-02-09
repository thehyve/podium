package org.bbmri.podium.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Table(name = "podium_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Authority implements Serializable {

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String PODIUM_ADMIN                 = "ROLE_PODIUM_ADMIN";
    public static final String BBMRI_ADMIN                  = "ROLE_BBMRI_ADMIN";
    public static final String ORGANISATION_ADMIN           = "ROLE_ORGANISATION_ADMIN";
    public static final String ORGANISATION_COORDINATOR     = "ROLE_ORGANISATION_COORDINATOR";
    public static final String REVIEWER                     = "ROLE_REVIEWER";
    public static final String RESEARCHER                   = "ROLE_RESEARCHER";

    public static final Set<String> ORGANISATION_AUTHORITIES = new HashSet<>(3);
    {
        ORGANISATION_AUTHORITIES.add(ORGANISATION_ADMIN);
        ORGANISATION_AUTHORITIES.add(ORGANISATION_COORDINATOR);
        ORGANISATION_AUTHORITIES.add(REVIEWER);
    }

    private static final long serialVersionUID = 1L;


    @NotNull
    @Size(min = 0, max = 50)
    @Id
    @Column(length = 50)
    private String name;

    public Authority() {}

    public Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final boolean isOrganisationAuthority(String name) {
        return ORGANISATION_COORDINATOR.contains(name);
    }

    public final boolean isOrganisationAuthority() {
        return isOrganisationAuthority(this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Authority authority = (Authority) o;

        if (name != null ? !name.equals(authority.name) : authority.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Authority{" +
            "name='" + name + '\'' +
            "}";
    }
}
