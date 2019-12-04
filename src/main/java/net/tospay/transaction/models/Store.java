package net.tospay.transaction.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.UserType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "user_id",
        "user_type",
        "account",
        "amount"
})
public class Store implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;


    @JsonProperty("account")
    private Account account;

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Amount getCharge()
    {
        return charge;
    }

    public void setCharge(Amount charge)
    {
        this.charge = charge;
    }

    public Amount getTotal()
    {
        return total;
    }

    public void setTotal(Amount total)
    {
        this.total = total;
    }

    @JsonProperty("charge")
    private Amount charge;

    @JsonProperty("total")
    private Amount total;


}