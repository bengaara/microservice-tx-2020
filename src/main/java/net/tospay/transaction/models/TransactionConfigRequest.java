package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import javax.persistence.Column;
import lombok.Data;
import lombok.experimental.Accessors;
import net.tospay.transaction.enums.MakerCheckerStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class TransactionConfigRequest extends BaseModel {

  //transaction.id
  @JsonProperty("id")
  UUID id;

  @Column(name = "airtime_limit")
  private BigDecimal airtimeLimit;

  @Column(name = "daily_transfer_limit")
  private BigDecimal dailyTransferLimit;

  @Column(name = "reversal_approval_count")
  private Integer reversalApprovalCount ;
  @Column(name = "transaction_approval_count")
  private Integer transactionApprovalCount;

  @Column(name = "evalue_approval_count")
  private Integer evalueApprovalCount;

  @JsonProperty("userInfo")
  private UserInfo userInfo;
  @JsonProperty("action")
  private MakerCheckerStatus action;


}