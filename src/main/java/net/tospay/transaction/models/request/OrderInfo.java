package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.models.Amount;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfo
{
    @JsonProperty("type")
    private OrderType type;

    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("reference")
    private String reference;

    public OrderType getType()
    {
        return type;
    }

    public void setType(OrderType type)
    {
        this.type = type;
    }

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }
}