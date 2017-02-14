package org.bbmri.podium.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccountNotVerifiedException extends DisabledException {
    public AccountNotVerifiedException(String msg) {
        super(msg);
    }

    public AccountNotVerifiedException(String msg, Throwable t) {
        super(msg, t);
    }
}
