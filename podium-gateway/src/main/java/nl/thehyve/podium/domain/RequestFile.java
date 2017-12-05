package nl.thehyve.podium.domain;

import lombok.AccessLevel;
import lombok.Setter;
import nl.thehyve.podium.common.domain.AbstractAuditingEntity;
import nl.thehyve.podium.enumeration.RequestFileType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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

    @Column(nullable = false)
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
        //
    }

    @PrePersist
    public void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String file_location) {
        this.fileLocation = file_location;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public RequestFileType getRequestFileType() {
        return requestFileType;
    }

    public void setRequestFileType(RequestFileType requestFileType) {
        this.requestFileType = requestFileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileByteSize() {
        return fileByteSize;
    }

    public void setFileByteSize(Long fileByteSize) {
        this.fileByteSize = fileByteSize;
    }

    public RequestFile copy(RequestFile requestFile){
        this.setOwner(requestFile.getOwner());
        this.setFileByteSize(requestFile.getFileByteSize());
        this.setFileName(requestFile.getFileName());
        this.setRequestFileType(requestFile.getRequestFileType());
        this.setFileLocation(requestFile.getFileLocation());
        this.setRequest(requestFile.getRequest());
        this.setOwner(requestFile.getOwner());
        this.generateUuid();
        return this;
    }
}
