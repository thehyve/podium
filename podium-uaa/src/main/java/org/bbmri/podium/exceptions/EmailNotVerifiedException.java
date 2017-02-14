package org.bbmri.podium.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class EmailNotVerifiedException extends DisabledException {
    public EmailNotVerifiedException(String msg) {
        super(msg);
    }

    public EmailNotVerifiedException(String msg, Throwable t) {
        super(msg, t);
    }
}
