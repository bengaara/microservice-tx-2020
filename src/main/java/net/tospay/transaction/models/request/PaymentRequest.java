package net.tospay.transaction.models.request;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.TransactionStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentRequest
{

    @JsonProperty("email") String email;

    @JsonProperty("merchant") UUID merchant;

    @JsonProperty("status") TransactionStatus status;

    @JsonProperty("reference") String reference;

    @JsonProperty("transaction_id") String transactionId;

    @JsonProperty("sender_id") String senderId;

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

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

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
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