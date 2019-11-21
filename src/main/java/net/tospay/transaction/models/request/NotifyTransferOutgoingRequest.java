package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.Transfer;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotifyTransferOutgoingRequest
{
    @JsonProperty("category")
    Notify.Category category;

    public Notify.Category getCategory()
    {
        return category;
    }

    public void setCategory(Notify.Category category)
    {
        this.category = category;
    }

    @JsonProperty("topic") Transfer.TransactionType topic;

    @JsonProperty("status") Transfer.TransactionStatus status;

    @JsonProperty("amount") String amount;
    @JsonProperty("currency") String currency;
  //  @JsonProperty("reference") String reference;
  //  @JsonProperty("date") String date;
    @JsonProperty("recipient_id") String recipientId;
    @JsonProperty("recipient_type") String recipientType;

    public Transfer.TransactionType getTopic()
    {
        return topic;
    }

    public void setTopic(Transfer.TransactionType topic)
    {
        this.topic = topic;
    }

    public Transfer.TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(Transfer.TransactionStatus status)
    {
        this.status = status;
    }

    public String getAmount()
    {
        return amount;
    }

    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getRecipientId()
    {
        return recipientId;
    }

    public void setRecipientId(String recipientId)
    {
        this.recipientId = recipientId;
    }

    public String getRecipientType()
    {
        return recipientType;
    }

    public void setRecipientType(String recipientType)
    {
        this.recipientType = recipientType;
    }
}