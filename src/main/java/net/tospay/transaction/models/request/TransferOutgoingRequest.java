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

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.MobilePayAction;

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
    private MobilePayAction action;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_type")
    private AccountType userType;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("charges")
    private Double charge;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("external_reference")
    private String externalReference;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Double getCharge()
    {
        return charge;
    }

    public void setCharge(Double charge)
    {
        this.charge = charge;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getExternalReference()
    {
        return externalReference;
    }

    public void setExternalReference(String externalReference)
    {
        this.externalReference = externalReference;
    }

    public MobilePayAction getAction()
    {
        return action;
    }

    public void setAction(MobilePayAction action)
    {
        this.action = action;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public AccountType getUserType()
    {
        return userType;
    }

    public void setUserType(AccountType userType)
    {
        this.userType = userType;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
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