package net.tospay.transaction.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import net.tospay.transaction.configs.BigDecimalMoneyDeserializer;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
        "amount",
        "currency"
})
public class Amount implements Serializable
{
    private final static long serialVersionUID = -4737973646400075962L;

    @JsonDeserialize(using = BigDecimalMoneyDeserializer.class )
    //@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,defaultImpl =BigDecimal.class )
    //  @JsonDeserialize(as = BigDecimal.class)
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Amount()
    {

    }

    public Amount(BigDecimal amount, String currency)
    {
        this.amount = amount;
        this.currency = currency;
    }

    @JsonProperty("amount")
    public BigDecimal getAmount()
    {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Amount withAmount(BigDecimal amount)
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