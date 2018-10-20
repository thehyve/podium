/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nl.thehyve.podium.common.config.PodiumConstants;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.validator.constraints.Email;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A user.
 */
@Entity
@Table(name = "podium_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class User extends AbstractAuditingEntity implements AuthenticatedUser, UserDetails, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @GenericGenerator(
        name = "user_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "user_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @NotNull
    @Pattern(regexp = PodiumConstants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash",length = 60)
    private String password;

    @Size(max = 50)
    @Column(name = "first_name", length = 50)
    private String firstName;

    @Size(max = 50)
    @Column(name = "last_name", length = 50)
    private String lastName;

    @Email
    @NotNull
    @Size(min=3, max = 100)
    @Column(length = 100, unique = true)
    private String email;

    @Column(name="telephone")
    private String telephone;

    @Column(name="institute")
    private String institute;

    @Column(name="department")
    private String department;

    @Column(name="job_title")
    private String jobTitle;

    @Column(name="specialism")
    private String specialism;

    @Size(min = 2, max = 5)
    @Column(name = "lang_key", length = 5)
    private String langKey = "en";

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Column(name = "activation_key_date", nullable = true)
    private ZonedDateTime activationKeyDate = null;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    private String resetKey;

    @Column(name = "reset_date", nullable = true)
    private ZonedDateTime resetDate = null;

    @Column(name="deleted")
    @Getter(AccessLevel.NONE)
    private boolean deleted = false;

    @Column(name="email_verified")
    @Getter(AccessLevel.NONE)
    private boolean emailVerified = false;

    @Column(name="admin_verified")
    @Getter(AccessLevel.NONE)
    private boolean adminVerified = false;

    @Column(name="failed_login_attempts")
    private int failedLoginAttempts = 0;

    @Column(name="account_locked")
    @Getter(AccessLevel.NONE)
    private boolean accountLocked = false;

    @Column(name = "account_lock_date", nullable = true)
    private ZonedDateTime accountLockDate = null;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_users",
        joinColumns = {@JoinColumn(name = "users_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "roles_id", referencedColumnName = "id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    private Set<Role> roles = new HashSet<>();

    public UUID getUserUuid() { return getUuid(); }

    /**
     * UUID is generated by the DBMS and cannot be overridden.
     * @param uuid This will be passed by syntax is satisfies for Hibernate and used mappers.
     */
    public void setUuid(UUID uuid) {
        // pass
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    // Lowercase the login before saving it in database
    public void setLogin(String login) {
        this.login = login.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getUsername() {
        return getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEmailVerified() && isAdminVerified();
    }

    @Override
    public Collection<String> getAuthorityNames() {
        return getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    public void setEmail(String email) {
        // Lowercase e-mail address before saving
        this.email = email.toLowerCase(Locale.ENGLISH);
    }

    public boolean isActivated() {
        return emailVerified && adminVerified;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAdminVerified() {
        return adminVerified;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void increaseFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return roles.stream()
            .map(Role::getAuthority)
            .map(Authority::getName)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    @Override
    public Map<UUID, Collection<String>> getOrganisationAuthorities() {
        Map<UUID, Collection<String>> result = new HashMap<>();
        for(Role role: getRoles()) {
            if (role.getOrganisation() != null) {
                UUID organisationUuid = role.getOrganisation().getUuid();
                Collection<String> organisationAuthorities = result.get(organisationUuid);
                if (organisationAuthorities == null) {
                    organisationAuthorities = new HashSet<>();
                    result.put(organisationUuid, organisationAuthorities);
                }
                organisationAuthorities.add(role.getAuthority().getName());
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        User user = (User) o;

        if (!login.equals(user.login)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }

    @Override
    public String getName() {
        return login;
    }

    @Override
    public String toString() {
        return "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", activated='" + isActivated() + '\'' +
            ", langKey='" + langKey + '\'' +
            ", activationKey='" + activationKey + '\'' +
            "}";
    }

}
