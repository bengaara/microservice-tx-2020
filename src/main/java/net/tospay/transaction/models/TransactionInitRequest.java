package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.MakerCheckerStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TransactionInitRequest extends BaseModel {

  @JsonProperty("fraudInfo")
  FraudInfo fraudInfo;

  //transaction.id
  @JsonProperty("id")
  UUID id;

  @JsonProperty("checker_stage")
  Integer checkerStage;



  @JsonProperty("from")
  LocalDate from;
  @JsonProperty("to")
  LocalDate to;
  @JsonProperty("userInfo")
  private UserInfo userInfo;
  @JsonProperty("reason")
  private String reason;
  @JsonProperty("action")
  private MakerCheckerStatus action;

  @JsonProperty("remarks")
  private String remarks;

  @JsonProperty("offset")
  private Integer offset = 0;
  @JsonProperty("limit")
  private Integer limit = 10;

}