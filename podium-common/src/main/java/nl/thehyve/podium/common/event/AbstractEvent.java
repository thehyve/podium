package nl.thehyve.podium.common.event;

import nl.thehyve.podium.common.security.AuthenticatedUser;

import java.util.Date;

public abstract class AbstractEvent {

    private AuthenticatedUser user;

    private EventType type;

    private Date eventDate = new Date();

    public AbstractEvent(AuthenticatedUser user, EventType type) {
        this.user = user;
        this.type = type;
    }

    public String getUsername() {
        if (user == null) {
            return null;
        }
        return user.getName();
    }

    public EventType getType() {
        return type;
    }

    public Date getEventDate() {
        return eventDate;
    }

}
