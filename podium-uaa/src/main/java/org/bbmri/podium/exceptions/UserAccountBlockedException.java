package org.bbmri.podium.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class UserAccountBlockedException extends LockedException {

    public UserAccountBlockedException(String msg) {
        super(msg);
    }

    public UserAccountBlockedException(String msg, Throwable t) {
        super(msg, t);
    }
}
