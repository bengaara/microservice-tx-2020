package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import lombok.Data;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.models.ReverseRequest;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.util.Utils;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "reversal")
@JsonIgnoreProperties
@Data
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class Reversal extends BaseEntity<UUID> {

  public static final String DATE_CREATED = "date_created";
  private static final String DATE_MODIFIED = "date_modified";
  private static final String ID = "id";


  @Id
 @Column(name = ID)
  //@GeneratedValue
  @Type(type = "org.hibernate.type.PostgresUUIDType")
  private UUID id;
  @Column(name = DATE_CREATED, nullable = false)
  private LocalDateTime dateCreated;
  @Column(name = DATE_MODIFIED)
  private LocalDateTime dateModified;

//  @MapsId("transaction")
//  @OneToOne
//  private Transaction transaction;

  //@JsonManagedReference
  @JsonBackReference
  @MapsId("reversalTransaction")
  @OneToOne( cascade={CascadeType.MERGE, CascadeType.PERSIST})
  private Transaction reversalTransaction;

  @Column(name = "reverse_charge")
  private boolean reverseCharge;
  @Column(name = "amount")
  private BigDecimal amount;
  @Column(name = "reason", nullable = true)
  private String reason;
  @Column(name = "payload", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private ReverseRequest payload;
  @Column(name = "maker")
  @Type(type = "jsonb")
  private UserInfo maker;
  @Column(name = "approval_count")
  private Integer approvalCount = 1;

  @Enumerated(EnumType.STRING)
  @Column(name = "mc_status")
  private MakerCheckerStatus MCStatus;

  @Column(name = "checkerRecords", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<ReverseRequest> checkerRecords = new ArrayList<>();

  public void addCheckerRecord(ReverseRequest record) {
    if (checkerRecords == null) {
      checkerRecords = new ArrayList<>();
    }
    checkerRecords.add(record);
  }

  @Override
  public String toString() {
    String s = "";
    if (!hideData) {
      s = Utils.inspect(this);
    } else {
      s = super.toString();
    }

    return s;
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
