package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.UserInfo;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransactionCommissionRequest extends BaseModel {
    @JsonProperty("transaction_id")
    String transactionId;

    @JsonProperty("user_id")
    UUID userId;
    //transaction.id
    @JsonProperty("id")
    UUID id;
    @JsonProperty("userInfo")
    UserInfo userInfo;


    public boolean calculateForOne() {
        return (this.getId() != null || this.getTransactionId() != null || this.getUserId() != null);
    }
}