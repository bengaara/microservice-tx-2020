package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.MakerCheckerStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ReverseRequest extends BaseModel {

    @JsonProperty("reverse_id")
    UUID reverseId;
    @JsonProperty("transaction_id")
    String transactionId;
    //transaction.id
    @JsonProperty("id")
    UUID id;

    @JsonProperty("checker_stage")
    Integer checkerStage;


    @JsonProperty("userInfo")
    UserInfo userInfo;
    @JsonProperty("payment_id")
    String paymentId;
    @JsonProperty("amount")
    BigDecimal amount;
    @JsonProperty("reason")
    String reason;
    @JsonProperty("reverseCharge")
    Boolean reverseCharge = false;
    @JsonProperty("action")
    MakerCheckerStatus action;
    @JsonProperty("offset")
    Integer offset = 0;
    @JsonProperty("limit")
    Integer limit = 10;
    @JsonProperty("from")
    LocalDate from;
    @JsonProperty("to")
    LocalDate to;

    public ReverseRequest(){

    }
}