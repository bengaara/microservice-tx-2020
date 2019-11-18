package net.tospay.transaction.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.databind.JsonNode;

import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.request.Destination;

@Entity
@Table(name = "transactions",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
public class Transaction extends BaseEntity<UUID> implements Serializable
{
    @Id
    @Column(name = "id", columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "payload", nullable = false)
    private JsonNode payload;

    @Column(name = "source_complete")
    private boolean sourceComplete;

    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus = TransactionStatus.CREATED;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    @Column(name = "date_modified", nullable = false)
    private Timestamp dateModified;

    //  mappedBy = "source",
    @OneToMany(

            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Source> sources = new ArrayList<>();

    @OneToMany(

            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Destination> destinations = new ArrayList<>();

    public Transaction()
    {
    }

    public boolean isSourceComplete()
    {
        return sourceComplete;
    }

    public void setSourceComplete(boolean sourceComplete)
    {
        this.sourceComplete = sourceComplete;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType)
    {
        this.transactionType = transactionType;
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

    public UUID getMerchantId()
    {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId)
    {
        this.merchantId = merchantId;
    }

    public JsonNode getPayload()
    {
        return payload;
    }

    public void setPayload(JsonNode payload)
    {
        this.payload = payload;
    }

    public TransactionStatus getTransactionStatus()
    {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus)
    {
        this.transactionStatus = transactionStatus;
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

    public List<Source> getSources()
    {
        return sources;
    }

    public void setSources(List<Source> sources)
    {
        this.sources = sources;
    }

    public List<Destination> getDestinations()
    {
        return destinations;
    }

    public void setDestinations(List<Destination> destinations)
    {
        this.destinations = destinations;
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
