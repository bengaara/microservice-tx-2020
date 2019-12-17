package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotifyTransferOutgoingSenderRequest
{
    @JsonProperty("sender_id") String senderId;

    @JsonProperty("sender_type") String senderType;

    @JsonProperty("receiver_id") String receiverId;

    @JsonProperty("receiver_type") String receiverType;

    public NotifyTransferOutgoingSenderRequest()
    {

    }

    public NotifyTransferOutgoingSenderRequest(String senderId, String senderType)
    {
        this.senderId = senderId;
        this.senderType = senderType;
    }

    public String getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(String receiverId)
    {
        this.receiverId = receiverId;
    }

    public String getReceiverType()
    {
        return receiverType;
    }

    public void setReceiverType(String receiverType)
    {
        this.receiverType = receiverType;
    }

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getSenderType()
    {
        return senderType;
    }

    public void setSenderType(String senderType)
    {
        this.senderType = senderType;
    }
}