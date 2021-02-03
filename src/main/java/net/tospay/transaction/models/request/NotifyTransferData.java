package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.StoreActionType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NotifyTransferData extends BaseModel {
    //  @JsonProperty("category") Notify.Category category;

    // public Notify.Category getCategory(){return category; }

    //   public void setCategory(Notify.Category category){this.category = category;}

    //NotifyTransferOutgoingRequest

    @JsonProperty("topic")
    TransactionType topic;

    @JsonProperty("status")
    TransactionStatus status;

    @JsonProperty("amount")
    Number amount;

    @JsonProperty("total")
    Number total;
    @JsonProperty("charge")
    Number charge;

    @JsonProperty("new_balance")
    Number newBalance;

    @JsonProperty("partnerRevenue")
    Number partnerRevenue;

    @JsonProperty("currency")
    String currency;
    @JsonProperty("reason")
    String reason;
    @JsonProperty("code")
    String code;
    @JsonProperty("description")
    String description;
    @JsonProperty("reference")
    String reference;
    @JsonProperty("transaction_id")
    String transactionId;
    @JsonProperty("id")
    UUID id;
    @JsonProperty("date")
    String date; //DD MMM YYYY hh:mm a

    @JsonProperty("senders")
    List<NotifyReferer> senders;
    @JsonProperty("receivers")
    List<NotifyReferer> receivers;
    @JsonProperty("referer")
    Account referer; //user_id,user_type
    @JsonProperty("utility")
    BaseModel utility; //user_id,user_type
    @JsonProperty("operation")
    private StoreActionType operation;

    @JsonProperty("hash")
    private String hash;
    @JsonProperty("channel")
    private String channel;


    @JsonProperty("action")
    private String action;

    @JsonProperty("preposition")
    private String preposition;


}