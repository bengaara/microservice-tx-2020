package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "user_id",
        "user_type",
        "account",
        "amount"
})
public class TransferOutgoingRequest implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("action")
    private String action;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Amount amount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }


    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    @JsonProperty("account")
    public Account getAccount()
    {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(Account account)
    {
        this.account = account;
    }

    public TransferOutgoingRequest withAccount(Account account)
    {
        this.account = account;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public TransferOutgoingRequest withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}