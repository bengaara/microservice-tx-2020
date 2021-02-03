package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class StoreResponse extends BaseModel  implements Serializable {
    @JsonProperty("channel")
    private AccountType channel;

    //    @JsonProperty("available_balance")
//    private BigDecimal availableBalance;
    @JsonProperty("prev_balance")
    private BigDecimal previousBalance;
    @JsonProperty("new_balance")
    private BigDecimal newBalance;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("amount")
    private Amount amount;
    @JsonProperty("user_type")
    private UserType userType;
    @JsonProperty("status")
    private TransactionStatus status;
    @JsonProperty("code")
    private String code;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("html")
    private String html;
    @JsonProperty("store_ref")
    private String storeRef;
    @JsonProperty("external_reference")
    private UUID externalReference;
    @JsonProperty("fx_id")
    private UUID fxId;

}