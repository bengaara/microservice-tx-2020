package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.models.TransactionConfigRequest;
import net.tospay.transaction.models.TransactionInitRequest;
import net.tospay.transaction.models.UserInfo;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "transaction_config",
    uniqueConstraints =
    @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class TransactionConfig extends BaseEntity<UUID> {

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

    @Column(name = "airtime_limit")
    private BigDecimal airtimeLimit = BigDecimal.valueOf(10000);

    @Column(name = "daily_transfer_limit")
    private BigDecimal dailyTransferLimit;

    @Column(name = "reversal_approval_count")
    private Integer reversalApprovalCount = 1;
    @Column(name = "transaction_approval_count")
    private Integer transactionApprovalCount = 1;

    @Column(name = "evalue_approval_count")
    private Integer evalueApprovalCount = 3;

    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private TransactionConfigRequest payload;

    @Column(name = "maker")
    @Type(type = "jsonb")
    private UserInfo maker;

    @Column(name = "approval_count")
    private Integer approvalCount = 1;
    @Column(name = "record", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private TransactionConfig record;

    @Enumerated(EnumType.STRING)
    @Column(name = "mc_status")
    private MakerCheckerStatus MCStatus;


    @Column(name = "checkerRecords", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private List<TransactionInitRequest> checkerRecords = new ArrayList<>();

    public static TransactionConfig init() {
        TransactionConfig model = new TransactionConfig();
        model.setAirtimeLimit(BigDecimal.valueOf(10000));
        model.setReversalApprovalCount(1);
        model.setTransactionApprovalCount(1);
        model.setDailyTransferLimit(BigDecimal.valueOf(50000));
        model.setEvalueApprovalCount(3);

        return model;
    }

    public void addCheckerRecord(TransactionInitRequest record) {
        if (checkerRecords == null) {
            checkerRecords = new ArrayList<>();
        }

        checkerRecords.add(record);
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
