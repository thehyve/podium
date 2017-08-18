package nl.thehyve.podium.event;

import nl.thehyve.podium.common.enumeration.DeliveryStatus;
import nl.thehyve.podium.common.enumeration.OverviewStatus;
import nl.thehyve.podium.common.enumeration.RequestReviewStatus;
import nl.thehyve.podium.common.enumeration.RequestStatus;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.service.AuditService;
import nl.thehyve.podium.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;

@Component
public class StatusUpdateEventListener {

    private Logger log = LoggerFactory.getLogger(StatusUpdateEventListener.class);

    private AuditService auditService;
    private NotificationService notificationService;

    @Autowired
    StatusUpdateEventListener(AuditService auditService, NotificationService notificationService) {
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Async
    @TransactionalEventListener
    public void persistStatusUpdateEvent(StatusUpdateEvent event) {
        log.info("Publish event to audit log: {}", event);
        auditService.publishEvent(event);
    }

    @Async
    @TransactionalEventListener
    public void notifyUsersOfStatusUpdateEvent(StatusUpdateEvent event) {
        log.info("Notification handler for event: {}", event);
        if (event.getSourceStatus() == OverviewStatus.Draft &&
            event.getTargetStatus() == OverviewStatus.Validation) {
            // sent draft submission notification to organisation coordinators for this request
            notificationService.submissionNotificationToCoordinators(event.getRequestUuid());
        } else if (event.getTargetStatus() == OverviewStatus.Review) {
            // Send review requested notification to all reviewers for this request
            notificationService.reviewNotificationToReviewers(event.getRequestUuid());
        } else if (event.getTargetStatus() == OverviewStatus.Rejected) {
            notificationService.requestRejectedNotificationToRequester(event.getRequestUuid());
        } else if ( event.getSourceStatus() == OverviewStatus.Review &&
            event.getTargetStatus() != OverviewStatus.Revision) {
            // Send rejection email if rejected; send approval mail if approved
            notificationService.reviewProcessClosedNotificationToRequester(event.getRequestUuid());
        } else if (event.getTargetStatus() == OverviewStatus.Revision) {
            notificationService.revisionNotificationToRequester(event.getRequestUuid());
        } else if (event.getSourceStatus() == OverviewStatus.Revision &&
            event.getTargetStatus() == OverviewStatus.Validation) {
            // Send revision submitted emails to all organisation coordinators for this request
            notificationService.revisionNotificationToCoordinators(event.getRequestUuid());
        } else if (event.getSourceStatus() == DeliveryStatus.Preparation &&
            event.getTargetStatus() == DeliveryStatus.Released) {
            // Send delivery released email to the requester for this delivery
            notificationService.deliveryReleasedNotificationToRequester(event.getRequestUuid(), event
                .getDeliveryProcessUuid());
        } else if (Arrays.asList(OverviewStatus.Closed_Approved, OverviewStatus.Delivered, OverviewStatus
                .Partially_Delivered,
            OverviewStatus.Cancelled).contains(event.getTargetStatus())) {
            // Send request closed email to requester
            notificationService.requestClosedNotificationToRequester(event.getRequestUuid());
        } else if (event.getTargetStatus() == DeliveryStatus.Closed) {
            // Send delivery closed email for this delivery
            notificationService.deliveryClosedNotification(event.getRequestUuid(), event.getDeliveryProcessUuid());
        }
    }

}
