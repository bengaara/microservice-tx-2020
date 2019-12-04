package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.models.Amount;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "source",
        "destination",
        "amount"
})
public class TransactionRequest implements Serializable
{
    private final static long serialVersionUID = -5739806967313718851L;

    @JsonProperty("type")
    private String type;

    @JsonProperty("source")
    private Source source;

    @JsonProperty("destination")
    private net.tospay.transaction.models.request.Destination destination;

    @JsonProperty("amount")
    private Amount amount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("type")
    public String getType()
    {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type)
    {
        this.type = type;
    }

    public TransactionRequest withType(String type)
    {
        this.type = type;
        return this;
    }

    @JsonProperty("source")
    public Source getSource()
    {
        return source;
    }

    @JsonProperty("source")
    public void setSource(Source source)
    {
        this.source = source;
    }

    public TransactionRequest withSource(Source source)
    {
        this.source = source;
        return this;
    }

    @JsonProperty("destination")
    public Destination getDestination()
    {
        return destination;
    }

    @JsonProperty("destination")
    public void setDestination(Destination destination)
    {
        this.destination = destination;
    }

    public TransactionRequest withDestination(Destination destination)
    {
        this.destination = destination;
        return this;
    }

    @JsonProperty("amount")
    public Amount getAmount()
    {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public TransactionRequest withAmount(Amount amount)
    {
        this.amount = amount;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public TransactionRequest withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}