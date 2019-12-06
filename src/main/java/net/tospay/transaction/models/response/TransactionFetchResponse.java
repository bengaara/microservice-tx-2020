package net.tospay.transaction.models.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.util.Utils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionFetchResponse
{
    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("tId")
    private String tId;

    public String gettId()
    {
        return tId;
    }

    public void settId(String tId)
    {
        this.tId = tId;
    }

    @JsonProperty("transactionTransferId")
    private String transactionTransferId;//source/destination id
    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("source_channel")
    private AccountType sourceChannel;

    @JsonProperty("amount")
    private Number amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("charge")
    private String charge;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    public String getDateCreatedFormatted()
    {
        return dateCreatedFormatted;
    }

    public void setDateCreatedFormatted(String dateCreatedFormatted)
    {
        this.dateCreatedFormatted = dateCreatedFormatted;
    }

    @JsonProperty("date_created_formatted")
    private String dateCreatedFormatted;

    @JsonProperty("date_updated")
    private LocalDateTime dateUpdated;

    @JsonProperty("status")
    private String status;

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public AccountType getSourceChannel()
    {
        return sourceChannel;
    }

    public void setSourceChannel(AccountType sourceChannel)
    {
        this.sourceChannel = sourceChannel;
    }

    public Number getAmount()
    {
        return amount;
    }

    public void setAmount(Number amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public String getCharge()
    {
        return charge;
    }

    public void setCharge(String charge)
    {
        this.charge = charge;
    }

    public LocalDateTime getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated()
    {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated)
    {
        this.dateUpdated = dateUpdated;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public String getTransactionTransferId()
    {
        return transactionTransferId;
    }

    public void setTransactionTransferId(String transactionTransferId)
    {
        this.transactionTransferId = transactionTransferId;
    }

    public static TransactionFetchResponse from(Source s){
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.setAmount(s.getPayload().getTotal().getAmount());
        res.setCharge(String.valueOf(s.getPayload().getCharge().getAmount()));
        res.setAmount(s.getPayload().getTotal().getAmount());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDate()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId().toString());
        res.settId(s.getTransaction().getId().toString());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setType(s.getTransaction().getPayload().getType());
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }
    public static TransactionFetchResponse from(Destination s){
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.setAmount(s.getPayload().getTotal().getAmount());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDate()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId().toString());
        res.settId(s.getTransaction().getId().toString());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setType(s.getTransaction().getPayload().getType());
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }
}
