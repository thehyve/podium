/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserAccountException extends Exception {
    public UserAccountException(String msg) {
        super(msg);
    }

    public UserAccountException(String msg, Throwable t) {
        super(msg, t);
    }
}
