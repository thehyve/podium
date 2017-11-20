package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Setter;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "request_file",
    indexes = {
        @Index(name = "request_file_created_date_key", columnList = "created_date"),
    }
)
public class RequestFile extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_seq_gen")
    @GenericGenerator(
        name = "request_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    @Column(nullable = false)
    private UUID owner;

    @Column(nullable = false)
    private UUID request;

    @Column(unique=true, name="file_location")
    private String fileLocation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getRequest() {
        return request;
    }

    public void setRequest(UUID request) {
        this.request = request;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String file_location) {
        this.fileLocation = file_location;
    }
}
