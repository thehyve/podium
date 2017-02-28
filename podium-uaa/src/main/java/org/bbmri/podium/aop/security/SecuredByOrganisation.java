/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.aop.security;

import org.bbmri.podium.domain.Authority;

import java.lang.annotation.*;

/**
 * Mark a controller method as accessible for any authorised user
 * that has access to a certain organisation and has any of the authorities
 * in {@link #authorities()} for that organisation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByOrganisation {

    /**
     * The name of the parameter that holds the uuid of an organisation.
     *
     * This should not be combined with {@link #objectParameter()}.
     */
    String uuidParameter() default "";

    /**
     * The name of the parameter that holds an object that is associated with an organisation.
     * The object should implement the {@link org.bbmri.podium.common.IdentifiableOrganisation} interface.
     *
     * This should not be combined with {@link #uuidParameter()}.
     */
    String objectParameter() default "";

    /**
     * Authority names (see {@link org.bbmri.podium.domain.Authority}).
     *
     * Defaults to all organisation roles.
     */
    String[] authorities() default {Authority.ORGANISATION_ADMIN, Authority.ORGANISATION_COORDINATOR, Authority.REVIEWER};

}
