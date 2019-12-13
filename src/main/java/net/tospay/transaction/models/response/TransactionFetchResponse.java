package net.tospay.transaction.models.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.util.Utils;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionFetchResponse
{
    @JsonProperty("transactionId")
    private String transactionId;//friendly name

    @JsonProperty("tId")
    private UUID tId;

    @JsonProperty("transactionTransferId")
    private UUID transactionTransferId;//source/destination id

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("orderType")
    private OrderType orderType;//qr split etc..

    @JsonProperty("sourceChannel")
    private AccountType sourceChannel;

    @JsonProperty("amount")
    private Number amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("charge")
    private Number charge;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("profile_pic")
    private String profilePic;

    @JsonProperty("dateCreatedFormatted")
    private String dateCreatedFormatted;

    @JsonProperty("date_created")
    private LocalDateTime dateCreated;

    @JsonProperty("date_updated")
    private LocalDateTime dateUpdated;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("operation")
    private String operation;//debit/credit

    public static TransactionFetchResponse from(Source s)
    {
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.setAmount(s.getPayload().getTotal().getAmount());
        res.setCharge(s.getPayload().getCharge().getAmount());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDate()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId());
        res.settId(s.getTransaction().getId());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setType(s.getTransaction().getPayload().getType());
        res.setSubType(s.getTransaction().getPayload().getOrderInfo().getType());
        res.setAccountName(s.getPayload().getAccount().getName());
        res.setProfilePic(s.getPayload().getAccount().getProfilePic());
        res.setOperation("Debit");
        // res.setDescription();
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }

    public static TransactionFetchResponse from(Destination s)
    {
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.setAmount(s.getPayload().getTotal().getAmount());
        res.setCharge(s.getPayload().getCharge().getAmount());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setDateCreated(s.getDateCreated());
        res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated().toLocalDate()));
        res.setDateUpdated(s.getDateModified());
        res.setTransactionId(s.getTransaction().getTransactionId());
        res.setTransactionTransferId(s.getId());
        res.settId(s.getTransaction().getId());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setType(s.getTransaction().getPayload().getType());
        res.setSubType(s.getTransaction().getPayload().getOrderInfo().getType());
        res.setAccountName(s.getPayload().getAccount().getName());
        res.setProfilePic(s.getPayload().getAccount().getProfilePic());
        res.setOperation("Credit");
        // res.setDescription();
        res.setStatus(s.getTransactionStatus().name());

        return res;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getOperation()
    {
        return operation;
    }

    public void setOperation(String operation)
    {
        this.operation = operation;
    }

    public OrderType getOrderType()
    {
        return orderType;
    }

    public void setOrderType(OrderType orderType)
    {
        this.orderType = orderType;
    }

    public void setSubType(OrderType orderType)
    {
        this.orderType = orderType;
    }

    public String getAccountName()
    {
        return accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public String getProfilePic()
    {
        return profilePic;
    }

    public void setProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
    }

    public UUID gettId()
    {
        return tId;
    }

    public void settId(UUID tId)
    {
        this.tId = tId;
    }

    public void setDateCreatedFormatted(String dateCreatedFormatted)
    {
        this.dateCreatedFormatted = dateCreatedFormatted;
    }

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

    public Number getCharge()
    {
        return charge;
    }

    public void setCharge(Number charge)
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

    public UUID getTransactionTransferId()
    {
        return transactionTransferId;
    }

    public void setTransactionTransferId(UUID transactionTransferId)
    {
        this.transactionTransferId = transactionTransferId;
    }
}
