package net.tospay.transaction.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.AsyncCallbackResponse;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.StoreResponse;

@Entity
@Table(name = "sources",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
@JsonIgnoreProperties
public class Source extends BaseEntity<UUID> implements Serializable
{
    public static final String DATE_CREATED = "date_created";

    @Id
    @Column(name = "id", columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "payload", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Store payload;

    @Column(name = "date_refunded")
    private LocalDateTime dateRefunded;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus transactionStatus = TransactionStatus.CREATED;

    @Column(name = "response", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull private Map<LocalDateTime, StoreResponse> response = new HashMap<>();

    @ElementCollection
    @Column(name = "date_request")
    @NotNull private List<LocalDateTime> dateRequest = new ArrayList<>();

    @Column(name = "response_async", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @NotNull private Map<LocalDateTime, AsyncCallbackResponse> responseAsync = new HashMap<>();

    @Column(name = DATE_CREATED, nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_modified", nullable = false)
    private LocalDateTime dateModified;

    @ManyToOne
    @JoinColumn(name = "transaction")
    private net.tospay.transaction.entities.Transaction transaction;

    public Source()
    {
    }

    public LocalDateTime getDateRefunded()
    {
        return dateRefunded;
    }

    public void setDateRefunded(LocalDateTime dateRefunded)
    {
        this.dateRefunded = dateRefunded;
    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }

    public Store getPayload()
    {
        return payload;
    }

    public void setPayload(Store payload)
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

    public Map<LocalDateTime, StoreResponse> getResponse()
    {
        return response;
    }

    public List<LocalDateTime> getDateRequest()
    {
        return dateRequest;
    }

    public Map<LocalDateTime, AsyncCallbackResponse> getResponseAsync()
    {
        return responseAsync;
    }

    public LocalDateTime getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateModified()
    {
        return dateModified;
    }

    public void setDateModified(LocalDateTime dateModified)
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
        dateModified = LocalDateTime.now();//System.currentTimeMillis();
    }

    @PrePersist
    protected void prePersist()
    {
        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
        dateModified = dateCreated;
    }
}
