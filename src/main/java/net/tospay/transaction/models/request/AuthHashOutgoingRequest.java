package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.util.Utils;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AuthHashOutgoingRequest extends BaseModel {


    @JsonProperty("timestamp")
    Long timestamp;

    @JsonProperty("amount")
    Number amount;
    @JsonProperty("currency")
    String currency;

    @JsonProperty("user_id")
    UUID userId;
    @JsonProperty("hash")
    String hash;


}