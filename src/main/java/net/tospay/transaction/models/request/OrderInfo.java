package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrderInfo extends BaseModel {

    @JsonProperty("description")
    String description;
    @JsonProperty("collection_channels")
    String CollectionChannels;
    @JsonProperty("source")
    String source;//WEB?
    @JsonProperty("date")
    String date;
    @JsonProperty("transaction_id")
    String transactionId;
    @JsonProperty("reference")
    private String reference;
    @JsonProperty("type")
    private OrderType type;
    @JsonProperty("amount")
    private Amount amount;
    @JsonProperty("token")
    private String token;

    @JsonProperty("utility")
    private BaseModel utility;
    @JsonProperty("account_no")
    private String accountNo; //for paybill

    @JsonProperty("cell_id")
    private String cellId;

}