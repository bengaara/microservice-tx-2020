package net.tospay.transaction.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.databind.JsonNode;

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.SourceType;
import net.tospay.transaction.enums.TransactionStatus;

@Entity
@Table(name = "sources",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
public class Source extends BaseEntity<UUID> implements Serializable
{
    @Id
    @Column(name = "id", columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "type", nullable = false)
    private SourceType type;

    @Column(name = "user_type", nullable = false)
    private AccountType userType;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account", nullable = false)
    private String account;//account id

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "charge", nullable = false)
    private Double charge;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus = TransactionStatus.CREATED;

    @Column(name = "response", nullable = false)
    private JsonNode response;

    @Column(name = "response_async", nullable = false)
    private JsonNode responseAsync;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    @Column(name = "date_modified", nullable = false)
    private Timestamp dateModified;

    @ManyToOne
    @JoinColumn(name = "transaction")
    private Transaction transaction;

    public Source()
    {
    }

    public SourceType getType()
    {
        return type;
    }

    public void setType(SourceType type)
    {
        this.type = type;
    }

    public AccountType getUserType()
    {
        return userType;
    }

    public void setUserType(AccountType userType)
    {
        this.userType = userType;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    public Double getCharge()
    {
        return charge;
    }

    public void setCharge(Double charge)
    {
        this.charge = charge;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public TransactionStatus getTransactionStatus()
    {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus)
    {
        this.transactionStatus = transactionStatus;
    }

    public JsonNode getResponse()
    {
        return response;
    }

    public void setResponse(JsonNode response)
    {
        this.response = response;
    }

    public JsonNode getResponseAsync()
    {
        return responseAsync;
    }

    public void setResponseAsync(JsonNode responseAsync)
    {
        this.responseAsync = responseAsync;
    }

    public Timestamp getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(Timestamp dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public Timestamp getDateModified()
    {
        return dateModified;
    }

    public void setDateModified(Timestamp dateModified)
    {
        this.dateModified = dateModified;
    }

    public Transaction getTransaction()
    {
        return transaction;
    }

    public void setTransaction(Transaction transaction)
    {
        this.transaction = transaction;
    }

    @Override
    public UUID getId()
    {
        return id;
    }

    @Override
    public void setId(UUID id)
    {
        this.id = id;
    }

    @PreUpdate
    protected void preUpdate()
    {
        dateModified = new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void prePersist()
    {
        dateCreated = new Timestamp(System.currentTimeMillis());
        dateModified = dateCreated;
    }
}
