/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.security.annotations;

import nl.thehyve.podium.common.IdentifiableUser;

import java.lang.annotation.*;

/**
 * Mark a controller method as accessible only for the user
 * that is associated with the object that is being requested.
 *
 * E.g., "updating a request is allowed for the owner of the request":
 * <code>
 *     @SecuredByCurrentUser(objectParameter = "request")
 *     public RequestRepresentation updateRequest(RequestRepresentation request) {
 *         ...
 *     }
 * </code>
 * Or, "fetching a user account is allowed for the owner of the account":
 * <code>
 *     @SecuredByCurrentUser(uuidParameter = "userUuid")
 *     public UserRepresentation getAccountDetails(UUID userUuid) {
 *         ...
 *     }
 * </code>
 *
 * The object referred to by {@link #objectParameter()} should implement
 * {@link IdentifiableUser}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SecuredByCurrentUser {

    /**
     * The name of the parameter that holds the uuid of the current user.
     *
     * This should not be combined with {@link #objectParameter()}.
     */
    String uuidParameter() default "";

    /**
     * The name of the parameter that holds an object that is associated with a user.
     * The object should implement the {@link IdentifiableUser} interface.
     *
     * This should not be combined with {@link #uuidParameter()}.
     */
    String objectParameter() default "";

}
