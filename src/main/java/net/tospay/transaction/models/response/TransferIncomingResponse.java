package net.tospay.transaction.models.response;

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
import net.tospay.transaction.enums.Transfer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
})
public class TransferIncomingResponse implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("channel")
    private Transfer.SourceType channel;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_type")
    private AccountType userType;

    @JsonProperty("transaction")
    private TopupMobileTransactionResponse transaction;

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

    public Transfer.SourceType getChannel()
    {
        return channel;
    }

    public void setChannel(Transfer.SourceType channel)
    {
        this.channel = channel;
    }

    public TopupMobileTransactionResponse getTransaction()
    {
        return transaction;
    }

    public void setTransaction(TopupMobileTransactionResponse transaction)
    {
        this.transaction = transaction;
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

    public AccountType getUserType()
    {
        return userType;
    }

    public void setUserType(AccountType userType)
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

    public TransferIncomingResponse withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}