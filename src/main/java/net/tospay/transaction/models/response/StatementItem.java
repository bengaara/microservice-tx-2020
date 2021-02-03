package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class StatementItem {

    @JsonProperty("transactionId")
    public String transactionId;
    @JsonProperty("transactionStatus")
    public String transactionStatus;
    @JsonProperty("type")
    public String type;

    @JsonProperty("date")
    public LocalDateTime date;
    @JsonProperty("description")
    public String description;
    @JsonProperty("action")
    public String action;
    @JsonProperty("currency")
    public String currency;
    @JsonProperty("incoming")
    public Number incoming;
    @JsonProperty("outgoing")
    public Number outgoing;
    @JsonProperty("charge")
    public Number charge;
    @JsonProperty("runningBalance")
    public Number runningBalance;
    @JsonProperty("dateFormatted")
    public String dateFormatted;
    @JsonProperty("dateCreatedFormattedShort")
    public String dateCreatedFormattedShort;

    public static StatementItem from(UUID ownerId, TransactionFetchResponse transaction) {
        StatementItem item = new StatementItem();
        item.transactionId = transaction.getTransactionId();
        item.type = Objects.toString(transaction.getType());
        item.transactionStatus = transaction.getStatus().name();
        item.date = transaction.getDateCreated();
        item.dateFormatted = transaction.getDateCreatedFormatted();
        item.dateCreatedFormattedShort = transaction.getDateCreatedFormattedShort();
        item.description = transaction.getDescription();

        item.action = TransactionFetchResponse.getAction(ownerId,transaction.getTransaction());

        item.currency = transaction.getCurrency();
        //item.charge = transaction.getCharge();
        if (!transaction.getSource().isEmpty() && ownerId.equals(transaction.getSource().get(0).getUserId())) {//TODO: reversal has no source?
            item.outgoing = transaction.getAmount();
            item.charge = transaction.getSource().get(0).getCharge();

            item.runningBalance = transaction.getSource().get(0).getAvailableBalance();
        } else if(!transaction.getDestination().isEmpty()){
            item.incoming = transaction.getAmount();
            item.charge = transaction.getDestination().get(0).getCharge();
            item.runningBalance = transaction.getDestination().get(0).getAvailableBalance();
        }

        return item;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Number getIncoming() {
        return incoming;
    }

    public void setIncoming(Number incoming) {
        this.incoming = incoming;
    }

    public Number getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Number outgoing) {
        this.outgoing = outgoing;
    }

    public Number getCharge() {
        return charge;
    }

    public void setCharge(Number charge) {
        this.charge = charge;
    }

    public Number getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(Number runningBalance) {
        this.runningBalance = runningBalance;
    }

    public String getDateFormatted() {
        return dateFormatted;
    }

    public void setDateFormatted(String dateFormatted) {
        this.dateFormatted = dateFormatted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
