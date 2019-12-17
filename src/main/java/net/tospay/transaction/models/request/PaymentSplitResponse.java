package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSplitResponse  extends BaseModel
{
    @JsonProperty("pay")  boolean pay;

    @JsonProperty("orderInfo") Account account; //who initiated split

    @JsonProperty("amount") Amount amount;

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public boolean isPay()
    {
        return pay;
    }

    public void setPay(boolean pay)
    {
        this.pay = pay;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }
}