/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
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
@Data
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

    @Override
    @JsonIgnore
    public UUID getUserUuid() {
        return getUuid();
    }

    public String getFullName() {
        return this.getFirstName() + " " + this.getLastName();
    }
}
