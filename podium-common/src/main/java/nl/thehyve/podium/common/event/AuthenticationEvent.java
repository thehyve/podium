package nl.thehyve.podium.common.event;

import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;

import java.util.UUID;

public class AuthenticationEvent extends AbstractEvent {

    private final EventType eventType;
    private final UUID userUuid;
    private final UUID handlerUuid;
    private final MessageRepresentation message;

    public AuthenticationEvent(
        AuthenticatedUser user,
        EventType eventType,
        UUID userUuid,
        UUID handlerUuid) {
        super(user, EventType.Status_Change);
        this.eventType = eventType;
        this.userUuid = userUuid;
        this.handlerUuid = handlerUuid;
        this.message = null;
    }

    public AuthenticationEvent(
        AuthenticatedUser user,
        EventType eventType,
        UUID userUuid,
        MessageRepresentation message) {
        super(user, EventType.Status_Change);
        this.eventType = eventType;
        this.userUuid = userUuid;
        this.handlerUuid = null;
        this.message = message;
    }

    public AuthenticationEvent(
        AuthenticatedUser user,
        EventType eventType,
        UUID userUuid) {
        super(user, EventType.Status_Change);
        this.eventType = eventType;
        this.userUuid = userUuid;
        this.message = null;
        this.handlerUuid = null;
    }

    public EventType getEventType() {
        return eventType;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public UUID getHandlerUuid() {
        return handlerUuid;
    }

    public MessageRepresentation getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Authentication Event user: " + userUuid.toString() + " event: " + eventType.toString();
    }
}
