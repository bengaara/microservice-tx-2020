package net.tospay.transaction.models.response;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
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
    private String type;

    @JsonProperty("source_channel")
    private String sourceChannel;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("charge")
    private String charge;

    @JsonProperty("date_created")
    private Date dateCreated;

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
    private Date dateUpdated;

    @JsonProperty("status")
    private String status;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getSourceChannel()
    {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel)
    {
        this.sourceChannel = sourceChannel;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
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

    public Date getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated()
    {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated)
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
        res.setAmount(s.getAmount());
        res.setCharge(s.getCharge().toString());
        res.setCurrency(s.getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDateTime()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId().toString());
        res.settId(s.getTransaction().getId().toString());
        res.setSourceChannel(s.getType().name());
        res.setType(s.getTransaction().getTransactionType().name());
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }
    public static TransactionFetchResponse from(Destination s){
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.setAmount(s.getAmount());
        res.setCharge(s.getCharge().toString());
        res.setCurrency(s.getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDateTime()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId().toString());
        res.settId(s.getTransaction().getId().toString());
        res.setSourceChannel(s.getType().name());
        res.setType(s.getTransaction().getTransactionType().name());
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }
}
