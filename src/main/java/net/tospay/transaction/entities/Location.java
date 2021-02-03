package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.request.LicenseObject;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "gpslocation",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class Location extends BaseEntity<UUID> {

    private static final String ID = "id";

    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "longitude")
    private String longitude;
    @Column(name = "latitude")
    private String latitude;
    @Column(name = "cellid")
    private String cellid;

}
