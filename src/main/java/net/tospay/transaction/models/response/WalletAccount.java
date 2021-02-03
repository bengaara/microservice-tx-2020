package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WalletAccount extends BaseModel {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("balance")
    private Number balance;

    @JsonProperty("last_balance")
    private Number lastBalance;


}
