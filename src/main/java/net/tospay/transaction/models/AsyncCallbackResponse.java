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

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.TransactionStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AsyncCallbackResponse  extends BaseModel
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("transaction")
    Body transaction;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("channel")
    private AccountType channel;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("external_reference")
    private UUID externalReference;

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Body getTransaction()
    {
        return transaction;
    }

    public void setTransaction(Body transaction)
    {
        this.transaction = transaction;
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

    public AsyncCallbackResponse withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class Body implements Serializable
    {
        @JsonProperty("id")
        private String id;

        @JsonProperty("amount")
        private Amount amount;

        @JsonProperty("trunc")
        private String trunc;//last 4 digits of number?

        @JsonProperty("network")
        private String network;//eg safaricom

        @JsonProperty("status")
        private TransactionStatus status;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public Amount getAmount()
        {
            return amount;
        }

        public void setAmount(Amount amount)
        {
            this.amount = amount;
        }

        public String getTrunc()
        {
            return trunc;
        }

        public void setTrunc(String trunc)
        {
            this.trunc = trunc;
        }

        public String getNetwork()
        {
            return network;
        }

        public void setNetwork(String network)
        {
            this.network = network;
        }

        public TransactionStatus getStatus()
        {
            return status;
        }

        public void setStatus(TransactionStatus status)
        {
            this.status = status;
        }
    }
}