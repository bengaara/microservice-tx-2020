package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeUser extends BaseModel
{
    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("callback_url")
    private String callbackUrl;

    public String getCallbackUrl()
    {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl)
    {
        this.callbackUrl = callbackUrl;
    }

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
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