package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NotifyTransferOutgoingRequest extends BaseModel {

    @JsonProperty("notification_type")
    Notify.Category notificationType;

    @JsonProperty("status")
    TransactionStatus status;

    @JsonProperty("recipient_id")
    String recipientId;
    @JsonProperty("recipient_type")
    UserType recipientType;

    @JsonProperty("data")
    BaseModel data;

    @JsonProperty("callback_url")
    private String callbackUrl;


}