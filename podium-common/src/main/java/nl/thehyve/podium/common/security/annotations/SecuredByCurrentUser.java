/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security.annotations;

import nl.thehyve.podium.common.IdentifiableUser;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method as accessible only for the user
 * that is associated with the object that is being requested.
 *
 * E.g., "updating a request is allowed for the owner of the request":
 * <pre>
 * {@code
 *     {@literal @}SecuredByCurrentUser
 *     public RequestRepresentation updateRequest(@UserParameter RequestRepresentation request) {
 *         ...
 *     }
 * }
 * </pre>
 * Or, "fetching a user account is allowed for the owner of the account":
 * <pre>
 * {@code
 *     {@literal @}SecuredByCurrentUser
 *     public UserRepresentation getAccountDetails(@UserUuidParameter UUID userUuid) {
 *         ...
 *     }
 * }
 * </pre>
 *
 * The user UUID parameter of the controller can be indicated with the
 * {@link UserUuidParameter} annotation. Or, alternatively, an
 * {@link IdentifiableUser} object can be indicated
 * using the {@link UserParameter} annotation. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByCurrentUser {}
