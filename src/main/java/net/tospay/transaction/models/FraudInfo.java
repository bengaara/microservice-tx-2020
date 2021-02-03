package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.enums.FraudStatus;

import javax.validation.constraints.NotNull;
@Data
public class FraudInfo extends BaseModel {
    @NotNull
    @JsonProperty("fraudQuery")
    private String fraudQuery;

    @NotNull
    @JsonProperty("status")
    private FraudStatus status;

    @JsonProperty("callBackUrl")
    private String callBackUrl;
    @JsonProperty("statusCode")
    private String statusCode;
    @JsonProperty("reason")
    private String reason;

}