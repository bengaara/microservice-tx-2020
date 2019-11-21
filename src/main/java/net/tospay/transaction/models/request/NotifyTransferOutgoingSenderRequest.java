package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotifyTransferOutgoingSenderRequest
{
    @JsonProperty("sender_id") String senderId;

    @JsonProperty("sender_type") String senderType;

    public NotifyTransferOutgoingSenderRequest()
    {

    }

    public NotifyTransferOutgoingSenderRequest(String senderId, String senderType)
    {
        this.senderId = senderId;
        this.senderType = senderType;
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