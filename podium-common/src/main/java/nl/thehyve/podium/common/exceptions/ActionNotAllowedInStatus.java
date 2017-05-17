/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.common.exceptions;

import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ActionNotAllowedInStatus extends Exception {
    public ActionNotAllowedInStatus(String msg) {
        super(msg);
    }

    public ActionNotAllowedInStatus(String msg, Throwable t) {
        super(msg, t);
    }

    public static ActionNotAllowedInStatus forStatus(RequestStatus status) {
        return new ActionNotAllowedInStatus("Action not allowed in status: " + status.name());
    }

    public static ActionNotAllowedInStatus forStatus(RequestReviewStatus status) {
        return new ActionNotAllowedInStatus("Action not allowed in status: " + status.name());
    }

}
