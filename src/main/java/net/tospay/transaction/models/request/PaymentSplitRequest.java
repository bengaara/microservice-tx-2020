package net.tospay.transaction.models.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.response.MerchantInfo;
import net.tospay.transaction.models.response.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSplitRequest
{
    @JsonProperty("email") String email;

    @JsonProperty("merchant") UUID merchant;

    @JsonProperty("status") Transfer.TransactionStatus status;

    @JsonProperty("reference") String reference;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public UUID getMerchant()
    {
        return merchant;
    }

    public void setMerchant(UUID merchant)
    {
        this.merchant = merchant;
    }

    public Transfer.TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(Transfer.TransactionStatus status)
    {
        this.status = status;
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