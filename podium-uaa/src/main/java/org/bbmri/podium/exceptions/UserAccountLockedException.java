package org.bbmri.podium.exceptions;

import org.springframework.security.authentication.LockedException;

public class UserAccountLockedException extends LockedException {

    public UserAccountLockedException(String msg) {
        super(msg);
    }

    public UserAccountLockedException(String msg, Throwable t) {
        super(msg, t);
    }
}
