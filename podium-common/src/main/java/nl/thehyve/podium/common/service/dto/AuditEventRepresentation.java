package nl.thehyve.podium.common.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.boot.actuate.audit.AuditEvent;

import java.util.Date;
import java.util.Map;

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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
