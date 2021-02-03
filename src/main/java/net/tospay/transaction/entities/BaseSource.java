package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.Store;
import org.hibernate.annotations.Type;

@MappedSuperclass
@Data
public abstract class BaseSource extends BaseEntity<UUID> {

    public static final String DATE_CREATED = "date_created";
    private static final String DATE_MODIFIED = "date_modified";
    private static final String ID = "id";
    @Column(name = "available_balance")
    BigDecimal availableBalance;
    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    @Column(name = DATE_CREATED, nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = DATE_MODIFIED)
    private LocalDateTime dateModified;
    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Store payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus = TransactionStatus.CREATED;
    @Column(name = "response", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull
    private Map<LocalDateTime, Object> response = new HashMap<>();
    @Column(name = "notified", columnDefinition = "bool")
    private boolean notified = false;
    @Column(name = "response_async", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull
    private Map<LocalDateTime, Object> responseAsync = new HashMap<>();
    @Column(name = "request", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull
    private Map<LocalDateTime, Object> request = new HashMap<>();
    @ManyToOne
    @JoinColumn(name = "transaction")
    private net.tospay.transaction.entities.Transaction transaction;
    @Column(name = "reason")
    private String reason;
    @Column(name = "code")
    private String code;

    @Column(name = "store_ref")
    private String storeRef;

    @Column(name = "fx_id")
    private UUID fxId;

    @Column(name = "revenue", columnDefinition = "bool")
    private boolean revenue = false;
    @Column(name = "description")
    private String description;
    @Column(name = "store_status_check_count")
    private Integer storeStatusCheckCount = 0;

    public Integer getStoreStatusCheckCount() {
        if (storeStatusCheckCount == null) storeStatusCheckCount = 0;
        return storeStatusCheckCount;
    }

    @PreUpdate
    protected void preUpdate() {
        dateModified = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void prePersist() {
        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }
}
