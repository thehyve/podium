/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.service.representation;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.validation.Required;
import nl.thehyve.podium.config.Constants;

import nl.thehyve.podium.domain.User;

import org.hibernate.validator.constraints.Email;
import org.springframework.security.core.GrantedAuthority;

import javax.validation.constraints.*;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserRepresentation implements IdentifiableUser {

    @Pattern(regexp = Constants.LOGIN_REGEX)
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

    @Size(min = 2, max = 5)
    private String langKey;

    private Set<String> authorities;

    public UserRepresentation() {
    }

    public UserRepresentation(User user) {
        this.login = user.getLogin();
        this.uuid = user.getUuid();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.telephone = user.getTelephone();
        this.institute = user.getInstitute();
        this.department = user.getDepartment();
        this.jobTitle = user.getJobTitle();
        this.specialism = user.getSpecialism();
        this.emailVerified = user.isEmailVerified();
        this.adminVerified = user.isAdminVerified();
        this.accountLocked = user.isAccountLocked();
        this.langKey = user.getLangKey();
        this.authorities = user.getAuthorities().stream().map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public UUID getUuid() { return uuid; }

    public UUID getUserUuid() { return getUuid(); }

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
        return langKey;
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

    @Override
    public String toString() {
        return "UserDTO{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", langKey='" + langKey + '\'' +
            ", authorities=" + authorities +
            "}";
    }
}
