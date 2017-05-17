/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method as accessible for any authorised user with
 * any of the authorities in {@link #value()}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByAuthority {

    /**
     * Authority names. See {@link nl.thehyve.podium.common.security.AuthorityConstants}.
     *
     * @return String[] Array of authority names.
     */
    String[] value() default {};

}
