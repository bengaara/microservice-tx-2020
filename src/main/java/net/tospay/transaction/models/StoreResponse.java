package net.tospay.transaction.models;

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
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreResponse  extends BaseModel
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("channel")
    private AccountType channel;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("user_type")
    private UserType userType;

    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("external_reference")
    private UUID externalReference;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
    {
        this.status = status;
    }

    public AccountType getChannel()
    {
        return channel;
    }

    public void setChannel(AccountType channel)
    {
        this.channel = channel;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public UUID getExternalReference()
    {
        return externalReference;
    }

    public void setExternalReference(UUID externalReference)
    {
        this.externalReference = externalReference;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public UserType getUserType()
    {
        return userType;
    }

    public void setUserType(UserType userType)
    {
        this.userType = userType;
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

    public StoreResponse withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}