package nl.thehyve.podium.service;

import nl.thehyve.podium.client.InternalAuditClient;
import nl.thehyve.podium.event.StatusUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for audit logging.
 */
@Service
public class AuditService {

    @Autowired
    private InternalAuditClient internalAuditClient;

    private AuditEvent convert(StatusUpdateEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("sourceStatus", event.getSourceStatus());
        data.put("targetStatus", event.getTargetStatus());
        data.put("requestUuid", event.getRequestUuid());
        return new AuditEvent(
            event.getUsername(),
            event.getClass().getSimpleName(),
            data
        );
    }

    public void publishEvent(StatusUpdateEvent event) {
        internalAuditClient.publishEvent(convert(event));
    }

}
