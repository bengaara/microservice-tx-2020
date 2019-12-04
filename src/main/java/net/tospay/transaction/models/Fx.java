package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Fx
{
    @JsonProperty("multiplier") Double multiplier;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Amount amount;

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public Double getMultiplier()
    {
        return multiplier;
    }

    public void setMultiplier(Double multiplier)
    {
        this.multiplier = multiplier;
    }
}