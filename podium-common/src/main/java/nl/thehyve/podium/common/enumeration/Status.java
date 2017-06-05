package nl.thehyve.podium.common.enumeration;

import java.util.Arrays;

/**
 * Indicates a type as a status type, which should be an enum.
 */
public interface Status {

    /**
     * The name of the enum value.
     * @return the name of the enum value.
     */
    String name();

    /**
     * Checks if the current status is in the list of allowed statuses.
     * @param currentStatus the current status.
     * @param allowedStatuses the list of allowed statuses.
     * @param <S> the status type.
     * @return true iff the current status is in the list of allowed statuses.
     */
    static <S extends Status> boolean isCurrentStatusAllowed(S currentStatus, S ... allowedStatuses) {
        return Arrays.stream(allowedStatuses).anyMatch(status ->
            status == currentStatus
        );
    }

}
