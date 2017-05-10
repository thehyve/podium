package nl.thehyve.podium.event;

import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StatusUpdateEventListener {

    private Logger log = LoggerFactory.getLogger(StatusUpdateEventListener.class);

    @Autowired
    AuditService auditService;

    @EventListener
    public void persistStatusUpdateEvent(StatusUpdateEvent event) {
        log.info("Event fired: {}", event);
        auditService.publishEvent(event);
    }

    @EventListener
    public void notifyUsersOfStatusUpdateEvent(StatusUpdateEvent event) {

    }

}
