/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security.annotations;

import nl.thehyve.podium.common.IdentifiableRequest;

import java.lang.annotation.*;

/**
 * Mark a controller method as accessible for any authorised user
 * that is coordinator of one of the organisations to which the request is associated,
 * if the request is not in draft status.
 *
 * The request UUID parameter of the controller can be indicated with the
 * {@link RequestUuidParameter} annotation. Or, alternatively, an
 * {@link IdentifiableRequest} object can be indicated
 * using the {@link RequestParameter} annotation.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByRequestOrganisationCoordinator {}
