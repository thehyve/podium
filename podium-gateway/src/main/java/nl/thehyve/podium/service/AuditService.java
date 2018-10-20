package nl.thehyve.podium.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.thehyve.podium.client.InternalAuditClient;
import nl.thehyve.podium.common.event.StatusUpdateEvent;
import nl.thehyve.podium.common.service.dto.AuditEventRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for audit logging.
 */
@Service
public class AuditService {

    private Logger log = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private InternalAuditClient internalAuditClient;

    @Autowired(required = false)
    private Registration registration;

    @Value("${eureka.instance.appname}")
    String appName;

    private ObjectMapper mapper = new ObjectMapper();

    private AuditEventRepresentation convert(StatusUpdateEvent event) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sourceStatus", event.getSourceStatus());
        data.put("targetStatus", event.getTargetStatus());
        data.put("requestUuid", event.getRequestUuid());
        String serviceId = appName;
        if (registration != null && registration.getServiceId() != null) {
            serviceId = registration.getServiceId();
        }
        data.put("service", serviceId);
        AuditEventRepresentation representation = new AuditEventRepresentation();
        representation.setTimestamp(event.getEventDate());
        representation.setPrincipal(event.getUsername());
        representation.setType(event.getType().toString());
        representation.setData(data);
        return representation;
    }

    @Async
    public void publishEvent(StatusUpdateEvent event) {
        AuditEventRepresentation auditEvent = convert(event);
        try {
            log.info("Publishing event {}", mapper.writeValueAsString(auditEvent));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unexpected error", e);
        }
        internalAuditClient.add(auditEvent);
    }

}
