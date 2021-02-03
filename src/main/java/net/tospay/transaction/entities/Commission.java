package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.ReverseRequest;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.request.CommissionRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.CommissionResponse;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.util.Utils;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "commission")
@JsonIgnoreProperties
@Data
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id")
public class Commission extends BaseEntity<UUID> {

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

  @Column(name = "amount", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private Amount amount;

  @Column(name = "payload", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private CommissionRequest payload;

  @Column(name = "commissionResponse", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<CommissionResponse> commissionResponse;


  @Enumerated(EnumType.STRING)
  @JsonProperty("fetchStatus")
  private ResponseCode fetchStatus;



  @Enumerated(EnumType.STRING)
  @JsonProperty("processStatus")
  private ResponseCode processStatus;

  @Column(name = "failedRequests", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<TransferOutgoingRequest> failedRequests= new ArrayList<>();

  @JsonProperty("failed")
  private Boolean failed;

  @Column(name = "storeResponses", columnDefinition = "jsonb")
  @Type(type = "jsonb")
  private List<ResponseObject<StoreResponse>> storeResponses= new ArrayList<>();

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
