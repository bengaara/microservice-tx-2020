package net.tospay.transaction.models.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.AccountType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionFetchRequest
{

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("user_type")
    private AccountType userType;

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public AccountType getUserType()
    {
        return userType;
    }

    public void setUserType(AccountType userType)
    {
        this.userType = userType;
    }
}