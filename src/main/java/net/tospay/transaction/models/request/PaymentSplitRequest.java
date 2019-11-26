package net.tospay.transaction.models.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.Transfer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotifyPaymentOutgoingRequest
{


    @JsonProperty("email") String email;
    @JsonProperty("merchant") String merchant;

    @JsonProperty("reference") String reference;

    @JsonProperty("status") ResponseCode status;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getMerchant()
    {
        return merchant;
    }

    public void setMerchant(String merchant)
    {
        this.merchant = merchant;
    }

    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

    public ResponseCode getStatus()
    {
        return status;
    }

    public void setStatus(ResponseCode status)
    {
        this.status = status;
    }
}