package net.tospay.transaction.models.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotifyTransferOutgoingRequest
{
  //  @JsonProperty("category") Notify.Category category;

   // public Notify.Category getCategory(){return category; }

 //   public void setCategory(Notify.Category category){this.category = category;}

    @JsonProperty("topic") TransactionType topic;

    @JsonProperty("status") TransactionStatus status;

    @JsonProperty("amount") String amount;
    @JsonProperty("currency") String currency;

    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

    public String getDate()
    {
        return date;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    @JsonProperty("reference") String reference;
   @JsonProperty("date") String date; //DD MMM YYYY hh:mm a
    @JsonProperty("recipient_id") String recipientId;
    @JsonProperty("recipient_type") String recipientType;
    @JsonProperty("senders") List<NotifyTransferOutgoingSenderRequest> senders;

    public List<NotifyTransferOutgoingSenderRequest> getSenders()
    {
        return senders;
    }

    public void setSenders(List<NotifyTransferOutgoingSenderRequest> senders)
    {
        this.senders = senders;
    }

    public TransactionType getTopic()
    {
        return topic;
    }

    public void setTopic(TransactionType topic)
    {
        this.topic = topic;
    }

    public TransactionStatus getStatus()
    {
        return status;
    }

    public void setStatus(TransactionStatus status)
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