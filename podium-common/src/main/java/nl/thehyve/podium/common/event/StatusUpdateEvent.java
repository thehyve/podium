package nl.thehyve.podium.common.event;

import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.security.AuthenticatedUser;
import nl.thehyve.podium.common.service.dto.MessageRepresentation;

import java.util.UUID;

public class StatusUpdateEvent<S extends Status> extends AbstractEvent {

    private final S sourceStatus;
    private final S targetStatus;
    private final UUID requestUuid;
    private final UUID deliveryProcessUuid;
    private final MessageRepresentation message;

    public StatusUpdateEvent(
        AuthenticatedUser user,
        S sourceStatus,
        S targetStatus,
        UUID requestUuid,
        UUID deliveryProcessUuid,
        MessageRepresentation message) {
        super(user, EventType.Status_Change);
        this.sourceStatus = sourceStatus;
        this.targetStatus = targetStatus;
        this.requestUuid = requestUuid;
        this.deliveryProcessUuid = deliveryProcessUuid;
        this.message = message;
    }

    public StatusUpdateEvent(
        AuthenticatedUser user,
        S sourceStatus,
        S targetStatus,
        UUID requestUuid,
        MessageRepresentation message) {
        this(user, sourceStatus, targetStatus, requestUuid, null, message);
    }

    public StatusUpdateEvent(
        AuthenticatedUser user,
        S sourceStatus,
        S targetStatus,
        UUID requestUuid,
        UUID deliveryProcessUuid) {
        this(user, sourceStatus, targetStatus, requestUuid, deliveryProcessUuid, null);
    }

    public StatusUpdateEvent(
        AuthenticatedUser user,
        S sourceStatus,
        S targetStatus,
        UUID requestUuid) {
        this(user, sourceStatus, targetStatus, requestUuid, null, null);
    }

    public S getSourceStatus() {
        return sourceStatus;
    }

    public S getTargetStatus() {
        return targetStatus;
    }

    public UUID getRequestUuid() {
        return requestUuid;
    }

    public UUID getDeliveryProcessUuid() {
        return deliveryProcessUuid;
    }

    public MessageRepresentation getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (deliveryProcessUuid == null) {
            return "Status of request " + requestUuid.toString() + " has been updated: " +
                sourceStatus.toString() + " --> " + targetStatus.toString();
        } else {
            return "Status of delivery " + deliveryProcessUuid.toString() +
                " (request " + requestUuid.toString() + ")" +
                " has been updated: " +
                sourceStatus.toString() + " --> " + targetStatus.toString();
        }
    }

}
