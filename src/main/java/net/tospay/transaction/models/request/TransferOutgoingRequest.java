package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "user_id",
        "user_type",
        "account",
        "amount"
})
@Data
public class TransferOutgoingRequest extends BaseModel implements Serializable {
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("action")
    private String action;
    @JsonProperty("description")
    private String description;
    @JsonProperty("external_reference")
    private UUID externalReference;
    @JsonProperty("merchant_reference")
    private String merchantReference;
    @JsonProperty("account")
    private Account account;
    @JsonProperty("amount")
    private Amount amount;
    @JsonProperty("transaction_type")
    private TransactionType transactionType;
    @JsonProperty("fx_id")
    private UUID fxId;
    @JsonProperty("callback_url")
    private String callbackUrl;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    @JsonProperty("account")
    public void setAccount(Account account) {
        this.account = account;
    }

    public TransferOutgoingRequest withAccount(Account account) {
        this.account = account;
        return this;
    }

    @Override
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    @Override
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public TransferOutgoingRequest withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}