package nl.thehyve.podium.event;

import nl.thehyve.podium.common.security.AuthenticatedUser;

import java.util.Date;

public abstract class AbstractEvent {

    private AuthenticatedUser user;

    private Date eventDate = new Date();

    public AbstractEvent(AuthenticatedUser user) {
        this.user = user;
    }

    public String getUsername() {
        if (user == null) {
            return null;
        }
        return user.getName();
    }

    public Date getEventDate() {
        return eventDate;
    }

}
