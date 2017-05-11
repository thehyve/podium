package nl.thehyve.podium.common.event;

import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.security.AuthenticatedUser;

import java.util.UUID;

public class StatusUpdateEvent extends AbstractEvent {

    private final Status sourceStatus;
    private final Status targetStatus;
    private final UUID requestUuid;
    private final String message;

    public StatusUpdateEvent(
        AuthenticatedUser user,
        Status sourceStatus,
        Status targetStatus,
        UUID requestUuid,
        String message) {
        super(user, EventType.Status_Change);
        this.sourceStatus = sourceStatus;
        this.targetStatus = targetStatus;
        this.requestUuid = requestUuid;
        this.message = message;
    }

    public StatusUpdateEvent(
        AuthenticatedUser user,
        Status sourceStatus,
        Status targetStatus,
        UUID requestUuid) {
        this(user, sourceStatus, targetStatus, requestUuid, null);
    }

    public Status getSourceStatus() {
        return sourceStatus;
    }

    public Status getTargetStatus() {
        return targetStatus;
    }

    public UUID getRequestUuid() {
        return requestUuid;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Status of request " + requestUuid.toString() + " has been updated: " +
            sourceStatus.toString() + " --> " + targetStatus.toString();
    }

}
