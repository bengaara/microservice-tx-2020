package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettlementOutgoingRequest extends BaseModel {

    @JsonProperty("account")
    Account account;
    @JsonProperty("amount")
    Amount amount;

    @JsonProperty("description")
    String description;

    @JsonProperty("transaction_id")
    String transactionId;
    @JsonProperty("settlement_id")
    String settlementId;
    @JsonProperty("status")
    TransactionStatus status;

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

}