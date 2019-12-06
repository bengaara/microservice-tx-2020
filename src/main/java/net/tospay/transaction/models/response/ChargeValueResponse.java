package net.tospay.transaction.models.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "amount",
        "currency"
})
public class ChargeValueResponse implements Serializable
{
    private final static long serialVersionUID = -4737973646400075962L;

    @JsonProperty("amount")
    private Number amount;

    @JsonProperty("currency")
    private String currency;

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Number getAmount()
    {
        return amount;
    }

    public void setAmount(Number amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }
}