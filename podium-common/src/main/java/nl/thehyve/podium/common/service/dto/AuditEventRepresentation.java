package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.util.Date;
import java.util.Map;

@Data
public class AuditEventRepresentation {

    private Date timestamp;

    private String principal;

    private String type;

    private Map<String, Object> data;

    public AuditEventRepresentation() {

    }

    public AuditEventRepresentation(AuditEvent event) {
        this.timestamp = event.getTimestamp();
        this.principal = event.getPrincipal();
        this.type = event.getType();
        this.data = event.getData();
    }

    @JsonIgnore
    public AuditEvent asAuditEvent() {
        return new AuditEvent(timestamp, principal, type, data);
    }

}
