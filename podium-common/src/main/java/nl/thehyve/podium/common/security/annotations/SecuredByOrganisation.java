/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security.annotations;

import nl.thehyve.podium.common.IdentifiableOrganisation;
import nl.thehyve.podium.common.security.AuthorityConstants;

import java.lang.annotation.*;

/**
 * Mark a controller method as accessible for any authorised user
 * that has access to a certain organisation and has any of the authorities
 * in {@link #authorities()} for that organisation.
 *
 * The organisation UUID parameter of the controller can be indicated with the
 * {@link OrganisationUuidParameter} annotation. Or, alternatively, an
 * {@link IdentifiableOrganisation} object can be indicated
 * using the {@link OrganisationParameter} annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByOrganisation {

    /**
     * Authority names (see {@link AuthorityConstants}.).
     *
     * Defaults to all organisation roles.
     */
    String[] authorities() default {
        AuthorityConstants.ORGANISATION_ADMIN,
        AuthorityConstants.ORGANISATION_COORDINATOR,
        AuthorityConstants.REVIEWER};

}
