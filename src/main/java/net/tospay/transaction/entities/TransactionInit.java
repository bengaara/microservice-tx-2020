package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.models.FraudInfo;
import net.tospay.transaction.models.TransactionInitRequest;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.UserInfo;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "transaction_init",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class TransactionInit extends BaseEntity<UUID> {

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

    @OneToOne(optional = true)
    @JoinColumn(name = "childTransaction")
    private Transaction childTransaction;

    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private TransactionRequest payload;
    @Column(name = "user_info")
    @Type(type = "jsonb")
    private UserInfo userInfo;
    @Column(name = "maker")
    @Type(type = "jsonb")
    private UserInfo maker;
  @Column(name = "approval_count")
  private Integer approvalCount = 1;
  @Column(name = "record", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private TransactionInitRequest record;

  @Column(name = "checkerRecords", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<TransactionInitRequest> checkerRecords = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "mc_status")
  private MakerCheckerStatus MCStatus;
  @Column(name = "fraud_info", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private FraudInfo fraudInfo;

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
