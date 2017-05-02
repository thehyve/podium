package nl.thehyve.podium.event;

import nl.thehyve.podium.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class StatusUpdateEventListener {

    @Autowired
    AuditService auditService;

    @TransactionalEventListener
    public void persistStatusUpdateEvent(StatusUpdateEvent event) {
        auditService.publishEvent(event);
    }

    @TransactionalEventListener
    public void notifyUsersOfStatusUpdateEvent(StatusUpdateEvent event) {

    }

}
