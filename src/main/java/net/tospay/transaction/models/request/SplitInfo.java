package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.models.Amount;

public class SplitInfo
{
    @JsonProperty("amount")
    private Number amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("description")
    private String description;

    @JsonProperty("email")
    private String email;

    @JsonProperty("charge")
    private Amount charge;

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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Amount getCharge()
    {
        return charge;
    }

    public void setCharge(Amount charge)
    {
        this.charge = charge;
    }
}