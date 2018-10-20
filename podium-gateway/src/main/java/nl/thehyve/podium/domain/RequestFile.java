package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.common.enumeration.RequestFileType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "request_file",
    indexes = {
        @Index(name = "request_file_created_date_key", columnList = "created_date"),
    }
)
@Data
public class RequestFile extends AbstractAuditingEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "request_file_seq_gen")
    @GenericGenerator(
        name = "request_file_seq_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "request_file_seq"),
            @Parameter(name = "initial_value", value = "1000"),
            @Parameter(name = "increment_size", value = "50")
        }
    )
    private Long id;

    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(unique = true, nullable = false)
    @Setter(AccessLevel.NONE)
    private UUID uuid;

    private UUID owner;

    @OneToOne
    @JoinColumn(unique = true, name = "request", nullable=false)
    @Fetch(FetchMode.JOIN)
    private Request request;

    @Column(unique=true, name="file_location")
    private String fileLocation;

    @Column(name="deleted")
    private Boolean deleted = false;

    @Column(name="request_file_type")
    @Enumerated(EnumType.STRING)
    private RequestFileType requestFileType;

    @Column(name="file_name")
    private String fileName;

    @Column(name="file_byte_size")
    private Long fileByteSize;

    @Column(name="uploader")
    private UUID uploader;

    /**
     * Only the database can return the UUID from the stored entity
     * Pre-persist will add a {@link UUID} to the entity
     * This setter is only added to satisfy mapstruct e.g.
     *
     * @param uuid is ignored.
     */
    public void setUuid(UUID uuid) {
        // pass
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestFile requestFile = (RequestFile) o;
        if (requestFile.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, requestFile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "RequestFile{" +
                "id=" + id +
                ", fileName='" + fileName + "'" +
                '}';
    }

}
