package net.tospay.transaction.models.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
})
public class TransactionIdRequest extends BaseModel
{
    @JsonProperty("country_iso_2")
    private String country;

    @JsonProperty("transaction_type")
    private TransactionType transactionType;

    @JsonProperty("account_type")
    private UserType userType;

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType)
    {
        this.transactionType = transactionType;
    }

    public UserType getUserType()
    {
        return userType;
    }

    public void setUserType(UserType userType)
    {
        this.userType = userType;
    }
}