package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.TransactionType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
})
public class ChargeRequest implements Serializable
{
    private final static long serialVersionUID = -4737973646400075962L;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("source")
    private ChargeRequestSource source;

    @JsonProperty("destination")
    private ChargeRequestDestination destination;

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

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public ChargeRequestSource getSource()
    {
        return source;
    }

    public void setSource(ChargeRequestSource source)
    {
        this.source = source;
    }

    public ChargeRequestDestination getDestination()
    {
        return destination;
    }

    public void setDestination(ChargeRequestDestination destination)
    {
        this.destination = destination;
    }

    public Map<String, Object> getAdditionalProperties()
    {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }
}