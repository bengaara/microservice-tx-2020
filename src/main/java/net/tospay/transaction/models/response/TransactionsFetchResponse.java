package net.tospay.transaction.models.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionsFetchResponse
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


    @JsonProperty("date_created")
    private Date dateCreated;

    @JsonProperty("status")
    private String status;

    @JsonProperty("source")
    List<TransactionFetchResponse> source = new ArrayList<>();

    public List<TransactionFetchResponse> getSource()
    {
        return source;
    }

    public void setSource(List<TransactionFetchResponse> source)
    {
        this.source = source;
    }

    public List<TransactionFetchResponse> getDelivery()
    {
        return delivery;
    }

    public void setDelivery(List<TransactionFetchResponse> delivery)
    {
        this.delivery = delivery;
    }

    @JsonProperty("delivery")
    List<TransactionFetchResponse> delivery = new ArrayList<>();



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


    public Date getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated)
    {
        this.dateCreated = dateCreated;
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

}
