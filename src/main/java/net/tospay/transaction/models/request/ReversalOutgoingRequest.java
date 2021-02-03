package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.util.Utils;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReversalOutgoingRequest extends BaseModel {

    @JsonProperty("payment_id")
    String paymentId;
    @JsonProperty("amount")
    Amount amount;
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("status")
    private ResponseCode status;
    @JsonProperty("reference")
    private String reference;

    public ResponseCode getStatus() {
        return status;
    }

    public void setStatus(ResponseCode status) {
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }


}