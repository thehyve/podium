package org.bbmri.podium.service.representation;

import org.bbmri.podium.config.Constants;

import org.bbmri.podium.domain.Authority;
import org.bbmri.podium.domain.User;

import org.hibernate.validator.constraints.Email;

import javax.validation.constraints.*;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
public class UserRepresentation {

    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    private UUID uuid;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(max = 100)
    private String email;

    private String telephone;

    private String institute;

    private String department;

    private String jobTitle;

    private String specialism;

    private boolean activated = false;

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
        this.activated = user.isActivated();
        this.langKey = user.getLangKey();
        this.authorities = user.getAuthorities().stream().map(Authority::getName)
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

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public void setAuthorities(Set<String> authorities) {
        this.authorities = authorities;
    }

    public UUID getUuid() { return uuid; }

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

    public boolean isActivated() {
        return activated;
    }

    public String getLangKey() {
        return langKey;
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
            ", activated=" + activated +
            ", langKey='" + langKey + '\'' +
            ", authorities=" + authorities +
            "}";
    }
}
