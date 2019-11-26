package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderInfo
{
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("description")
    private String description;
    @JsonProperty("reference")
    private String reference;

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

    public Amount getCharge()
    {
        return charge;
    }

    public void setCharge(Amount charge)
    {
        this.charge = charge;
    }

    @JsonProperty("charge")
    private Amount charge;

  }