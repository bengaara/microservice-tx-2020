package net.tospay.transaction.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transactional;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.request.TransferRequest;

@Entity
@Table(name = "transactions",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
@JsonIgnoreProperties
public class Transaction extends BaseEntity<UUID> implements Serializable
{
    private static final String DATE_MODIFIED = "date_modified";

    private static final String ID = "id";

    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "transaction_id", nullable = true)

    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Transfer.TransactionType transactionType;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private TransferRequest payload;

    @Column(name = "source_complete")
    private boolean sourceComplete;

    @Column(name = "destination_started")
    private boolean destinationStarted;

    @Column(name = "destination_complete")
    private boolean destinationComplete;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Transfer.TransactionStatus transactionStatus = Transfer.TransactionStatus.CREATED;

    @Column(name = "date_created", nullable = false)
    private Timestamp dateCreated;

    @Column(name = DATE_MODIFIED, nullable = false)
    private Timestamp dateModified;

    @Column(name = "external_reference")
    private String externalReference;

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
    private List<net.tospay.transaction.entities.Destination> destinations = new ArrayList<>();

    public Transaction()
    {
    }

    public boolean isDestinationStarted()
    {
        return destinationStarted;
    }

    public void setDestinationStarted(boolean destinationStarted)
    {
        this.destinationStarted = destinationStarted;
    }

    public boolean isDestinationComplete()
    {
        return destinationComplete;
    }

    public void setDestinationComplete(boolean destinationComplete)
    {
        this.destinationComplete = destinationComplete;
    }

    public void addSource(Source s)
    {
        this.sources.add(s);
        s.setTransaction(this);
    }

    public void addDestination(Destination s)
    {
        this.destinations.add(s);
        s.setTransaction(this);
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

    public Transfer.TransactionType getTransactionType()
    {
        return transactionType;
    }

    public String getExternalReference()
    {
        return externalReference;
    }

    public void setExternalReference(String externalReference)
    {
        this.externalReference = externalReference;
    }

    public void setTransactionType(Transfer.TransactionType transactionType)
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

    public TransferRequest getPayload()
    {
        return payload;
    }

    public void setPayload(TransferRequest payload)
    {
        this.payload = payload;
    }

    public Transfer.TransactionStatus getTransactionStatus()
    {
        return transactionStatus;
    }

    public void setTransactionStatus(Transfer.TransactionStatus transactionStatus)
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

    @Transactional
    public List<Source> getSources()
    {
        return sources;
    }

    public void setSources(List<Source> sources)
    {
        this.sources = sources;
    }

    @Transactional
    public List<net.tospay.transaction.entities.Destination> getDestinations()
    {
        return destinations;
    }

    public void setDestinations(List<net.tospay.transaction.entities.Destination> destinations)
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
