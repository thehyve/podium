/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package org.bbmri.podium.exceptions;

import org.springframework.security.authentication.DisabledException;

public class AccountNotVerifiedException extends DisabledException {
    public AccountNotVerifiedException(String msg) {
        super(msg);
    }

    public AccountNotVerifiedException(String msg, Throwable t) {
        super(msg, t);
    }
}
