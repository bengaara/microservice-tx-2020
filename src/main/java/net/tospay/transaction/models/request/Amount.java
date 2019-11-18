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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amount",
        "currency"
})
public class Amount implements Serializable
{
    private final static long serialVersionUID = -4737973646400075962L;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("amount")
    public Double getAmount()
    {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    public Amount withAmount(Double amount)
    {
        this.amount = amount;
        return this;
    }

    @JsonProperty("currency")
    public String getCurrency()
    {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public Amount withCurrency(String currency)
    {
        this.currency = currency;
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

    public Amount withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}