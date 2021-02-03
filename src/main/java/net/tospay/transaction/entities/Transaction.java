package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.FraudInfo;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.UserInfo;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "transactions",
    uniqueConstraints =
    @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
@Accessors(chain = true)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class Transaction extends BaseEntity<UUID> {

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
  //  @JsonIgnore
  //@JsonManagedReference
  @ToString.Exclude
  @JsonBackReference
  @OneToOne(optional = true,fetch = FetchType.LAZY)
  @JoinColumn(name = "reversalParent")
  private Transaction reversalParent;
  @JsonManagedReference
  //@JsonBackReference
  @OneToOne(cascade = {CascadeType.ALL},
      fetch = FetchType.LAZY, mappedBy = "reversalParent")
  private Transaction reversalChild;

  @Column(name = "reversed")
  private Boolean reversed = false;

//  @OneToOne( mappedBy = "transaction",cascade={CascadeType.MERGE, CascadeType.PERSIST})
//  private Reversal reversal;
  
  @Column(name = "reverse_charge")
  private boolean reverseCharge = false;
  @Column(name = "reverse_amount")
  private BigDecimal reverseAmount;
  //@JsonManagedReference
  @ToString.Exclude
  @JsonBackReference
  @JsonIgnore
  @OneToOne(optional = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "commission_parent")
  private Transaction commissionParent;
  @JsonManagedReference
  //@JsonBackReference
  @OneToOne(cascade = {CascadeType.ALL},
      fetch = FetchType.LAZY, mappedBy = "commissionParent")
  private Transaction commissionChild;
  @Column(name = "transaction_id", nullable = true)
  private String transactionId;
  @Column(name = "code", nullable = true)
  private String code;
  @Column(name = "reason", nullable = true)
  private String reason;
  @Enumerated(EnumType.STRING)
  @JsonProperty("type")
  private TransactionType type;//redundancy but needed for when payload might b null like reversal time
  @Column(name = "payload", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private TransactionRequest payload;
  @Column(name = "userInfo", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private UserInfo userInfo; //creator of transaction
  @Column(name = "source_complete")
  private boolean sourceComplete;
  @Column(name = "destination_started")
  private boolean destinationStarted;
  @Column(name = "destination_complete")
  private boolean destinationComplete;
  @Column(name = "revenue_processed")
  private boolean revenueProcessed = false;
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private TransactionStatus transactionStatus = TransactionStatus.CREATED;
  @Column(name = "fraud_info", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private FraudInfo fraudInfo;
  @Column(name = "notifiedFraud", columnDefinition = "bool")
  private boolean notifiedFraud = false;
  @Column(name = "publishAccountResponse", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private LinkedHashMap<LocalDateTime, Object> publishAccountResponse = new LinkedHashMap<>();
  @Column(name = "commission")
  @Type(type = "boolean")
  private boolean commission = false;

  @Column(name = "commissionPreProcessedFull")
  @Type(type = "boolean")
  private Boolean commissionPreProcessedFull = false;

  @ToString.Exclude
  @JsonIgnore
  @Column(name = "exception")
  @Type(type = "jsonb")
  String exception;

  //  mappedBy = "source",
  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Source> sources = new ArrayList<>();
  @LazyCollection(LazyCollectionOption.FALSE)
  @OneToMany(

      cascade = CascadeType.ALL,
      orphanRemoval = true, fetch = FetchType.EAGER
  )
  private List<Destination> destinations = new ArrayList<>();

  public void addDestination(Destination destinationEntity) {
    destinations.add(destinationEntity);
    destinationEntity.setTransaction(this);
  }

  public void addSource(Source sourceEntity) {

    sources.add(sourceEntity);
    sourceEntity.setTransaction(this);
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
