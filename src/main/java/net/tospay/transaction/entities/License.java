package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.request.LicenseObject;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;

@Entity
@Table(name = "license",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class License extends BaseEntity<UUID> {

    public static final String DATE_CREATED = "date_created";
    private static final String DATE_MODIFIED = "date_modified";
    private static final String ID = "id";

    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    @Column(name = DATE_CREATED, nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = DATE_MODIFIED)
    private LocalDateTime dateModified;

    @Column(name = "deviceInfo", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private LicenseObject deviceInfo;
    @Column(name = "license")
    private String license;
    @Column(name = "startDate")
    private LocalDateTime startDate;
    @Column(name = "endDate")
    private LocalDateTime endDate;
    @Column(name = "degradeDate")
    private LocalDateTime degradeDate;
    @Column(name = "daysRunning")
    private int daysRunning = 0;
    @Column(name = "daysLicensed")
    private int daysLicensed = 0;
    @Column(name = "degradePeriod")
    private int degradePeriod = 0;
    @Column(name = "degradeItems", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Store degradeItems;
    @Column(name = "publishResponse")
    @Type(type = "jsonb")
    private Store publishResponse;
    @Column(name = "statusResponse", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull
    private LinkedHashMap<LocalDateTime, Object> statusResponse = new LinkedHashMap<>();

    @PreUpdate
    protected void preUpdate() {
        dateModified = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void prePersist() {
        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }

}
