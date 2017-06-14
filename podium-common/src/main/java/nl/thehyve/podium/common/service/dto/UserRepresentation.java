/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.config.PodiumConstants;
import nl.thehyve.podium.common.validation.Required;
import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static nl.thehyve.podium.common.config.PodiumConstants.DEFAULT_LOCALE;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserRepresentation implements IdentifiableUser {

    @Pattern(regexp = PodiumConstants.LOGIN_REGEX)
    @Size(max = 50)
    @Required
    private String login;

    private UUID uuid;

    @Size(max = 50)
    @Required
    private String firstName;

    @Size(max = 50)
    @Required
    private String lastName;

    @Email
    @Size(max = 100)
    @Required
    private String email;

    @Required
    private String telephone;

    @Required
    private String institute;

    @Required
    private String department;

    @Required
    private String jobTitle;

    @Required
    private String specialism;

    private boolean emailVerified;

    private boolean adminVerified;

    private boolean accountLocked;

    private Map<UUID, Collection<String>> organisationAuthorities;

    @Size(min = 2, max = 5)
    private String langKey;

    private Set<String> authorities;

    public UserRepresentation() {
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setInstitute(String institute) {
        this.institute = institute;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setSpecialism(String specialism) {
        this.specialism = specialism;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public void setAdminVerified(boolean adminVerified) {
        this.adminVerified = adminVerified;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    @JsonIgnore
    public UUID getUserUuid() {
        return getUuid();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getInstitute() {
        return institute;
    }

    public String getDepartment() {
        return department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getSpecialism() {
        return specialism;
    }

    public String getLangKey() {
        return langKey == null ? DEFAULT_LOCALE : langKey;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public boolean isAdminVerified() {
        return adminVerified;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public Map<UUID, Collection<String>> getOrganisationAuthorities() {
        return organisationAuthorities;
    }

    public void setOrganisationAuthorities(Map<UUID, Collection<String>> organisationAuthorities) {
        this.organisationAuthorities = organisationAuthorities;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", langKey='" + getLangKey() + '\'' +
            ", authorities=" + authorities +
            "}";
    }
}
