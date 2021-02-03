package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.BaseModel;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "status"
})
public class TransferResponse extends BaseModel {
    @JsonProperty("html")
    private String html;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("status")
    private TransactionStatus status;

    //transaction.id
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("reference")
    private String reference;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}