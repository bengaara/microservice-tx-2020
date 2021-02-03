package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.BaseModel;

import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PaymentRequest extends BaseModel {


    @JsonProperty("payment_id")
    String paymentId;

    @JsonProperty("transaction_id")
    String transactionId;

    @JsonProperty("status")
    TransactionStatus status;

    @JsonProperty("merchant_id")
    UUID merchantId;
    @JsonProperty("sender_id")
    String senderId;


    @JsonProperty("amount")
    private BigDecimal amount;


}