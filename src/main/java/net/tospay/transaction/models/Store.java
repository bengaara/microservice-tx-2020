package net.tospay.transaction.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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

    @JsonProperty("order")
    private Amount order;

    @JsonProperty("charge")
    private Amount charge;

    @JsonProperty("total")
    private Amount total;//add when source.. sub when destination

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public Amount getOrder()
    {
        return order;
    }

    public void setOrder(Amount order)
    {
        this.order = order;
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
}