package org.bbmri.podium.exceptions;

import org.springframework.security.authentication.DisabledException;

public class EmailNotVerifiedException extends DisabledException {
    public EmailNotVerifiedException(String msg) {
        super(msg);
    }

    public EmailNotVerifiedException(String msg, Throwable t) {
        super(msg, t);
    }
}
