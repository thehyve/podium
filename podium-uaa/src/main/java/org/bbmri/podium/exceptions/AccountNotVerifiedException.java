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
