package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.enums.TransactionStatus;
@Data
public class FraudCallback extends BaseModel {

    @JsonProperty("fraudQuery")
    private String fraudQuery;

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus;

    @JsonProperty("code")
    private String code;

    @JsonProperty("reason")
    private String reason;

}