package nl.thehyve.podium.security;

import nl.thehyve.podium.common.IdentifiableUser;
import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.enumeration.Status;
import nl.thehyve.podium.common.exceptions.AccessDenied;
import nl.thehyve.podium.common.exceptions.ActionNotAllowed;
import nl.thehyve.podium.domain.DeliveryProcess;
import nl.thehyve.podium.domain.Request;

/**
 * Helper class for access checks for requests.
 */
public class RequestAccessCheckHelper {

    /**
     * Checks if the user is the requester of the request.
     * @param user the current user.
     * @param request the request object.
     * @throws AccessDenied iff the current user is not the requester.
     */
    public static void checkRequester(IdentifiableUser user, Request request) {
        if (!request.getRequester().equals(user.getUserUuid())) {
            throw new AccessDenied("Access denied to request " + request.getUuid().toString());
        }
    }

    /**
     * Checks if the request has any of the allowed statuses
     * @param request the request object.
     * @param allowedStatuses the allowed statuses.
     * @throws ActionNotAllowed iff the request does not have any of the allowed statuses.
     */
    public static RequestStatus checkStatus(Request request, RequestStatus ... allowedStatuses) throws ActionNotAllowed {
        if (!Status.isCurrentStatusAllowed(request.getStatus(), allowedStatuses)) {
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        return request.getStatus();
    }

    /**
     * Checks if the request has one of the allowed review statuses.
     * @param request the request object.
     * @param allowedStatuses the allowed review statuses.
     * @throws ActionNotAllowed iff the request is not in a review status or does not have any of the
     * allowed review statuses.
     */
    public static RequestReviewStatus checkReviewStatus(Request request, RequestReviewStatus ... allowedStatuses) throws ActionNotAllowed {
        if (request.getStatus() != RequestStatus.Review) {
            throw ActionNotAllowed.forStatus(request.getStatus());
        }
        RequestReviewStatus currentReviewStatus = request.getRequestReviewProcess().getStatus();
        if (!Status.isCurrentStatusAllowed(currentReviewStatus, allowedStatuses)) {
            throw ActionNotAllowed.forStatus(currentReviewStatus);
        }
        return currentReviewStatus;
    }

    /**
     * Checks if the delivery process has any of the allowed statuses.
     * @param deliveryProcess the delivery process object.
     * @param allowedStatuses the allowed statuses.
     * @return the current status iff the delivery process has any of the allowed statuses.
     * @throws ActionNotAllowed iff the delivery process does not have any of the allowed statuses.
     */
    public static DeliveryStatus checkDeliveryStatus(DeliveryProcess deliveryProcess, DeliveryStatus... allowedStatuses) throws ActionNotAllowed {
        if (!Status.isCurrentStatusAllowed(deliveryProcess.getStatus(), allowedStatuses)) {
            throw ActionNotAllowed.forStatus(deliveryProcess.getStatus());
        }
        return deliveryProcess.getStatus();
    }

}
