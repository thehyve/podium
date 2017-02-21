package org.bbmri.podium.exceptions;

import org.springframework.security.authentication.LockedException;

public class UserAccountBlockedException extends LockedException {

    public UserAccountBlockedException(String msg) {
        super(msg);
    }

    public UserAccountBlockedException(String msg, Throwable t) {
        super(msg, t);
    }
}
