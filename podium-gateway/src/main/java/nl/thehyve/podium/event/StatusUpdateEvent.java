package nl.thehyve.podium.event;

import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.security.AuthenticatedUser;

import java.util.UUID;

public class StatusUpdateEvent extends AbstractEvent {

    private final Status sourceStatus;
    private final Status targetStatus;
    private final UUID requestUuid;

    public StatusUpdateEvent(
        AuthenticatedUser user,
        Status sourceStatus,
        Status targetStatus,
        UUID requestUuid) {
        super(user);
        this.sourceStatus = sourceStatus;
        this.targetStatus = targetStatus;
        this.requestUuid = requestUuid;
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

}
