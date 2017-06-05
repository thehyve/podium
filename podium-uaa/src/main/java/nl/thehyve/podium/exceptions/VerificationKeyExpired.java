/*
 * Copyright (c) 2017  The Hyve and respective contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the file LICENSE in the root of this repository.
 */

package nl.thehyve.podium.exceptions;

public class VerificationKeyExpired extends Throwable {

    public VerificationKeyExpired() {
        super();
    }

    public VerificationKeyExpired(String msg) {
        super(msg);
    }

    public VerificationKeyExpired(String msg, Throwable t) {
        super(msg, t);
    }
}
