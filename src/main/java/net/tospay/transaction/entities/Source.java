package net.tospay.transaction.entities;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import net.tospay.transaction.configs.HashMapConverter;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.request.Account;

@Entity
@Table(name = "sources",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
@JsonIgnoreProperties
public class Source extends BaseEntity<UUID> implements Serializable
{
    public static final String ID = "id";

    public static final String TYPE = "type";

    public static final String USER_TYPE = "user_type";

    public static final String USER_ID = "user_id";

    public static final String ACCOUNT = "account";

    public static final String AMOUNT = "amount";

    public static final String CHARGE = "charge";


    public static final String RESPONSE = "response";

    public static final String CURRENCY ="currency" ;

    public static final String STATUS = "status";

    public static final String RESPONSE_ASYNC = "response_async";

    public static final String TRANSACTION = "transaction";

    public static final String DATE_CREATED ="date_created" ;

    public static final String DATE_MODIFIED ="date_modified" ;

    public static final String DATE_RESPONSE = "date_response";


    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = TYPE, nullable = false)
    private Transfer.SourceType type;

    @Column(name = USER_TYPE, nullable = false)
    private AccountType userType;

    @Column(name = USER_ID, nullable = false)
    private UUID userId;

    @Column(name = ACCOUNT, columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private Account account;

    @Column(name = AMOUNT, nullable = false)
    private Double amount;

    @Column(name = CHARGE)
    private Double charge;

    @Column(name = CURRENCY, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = STATUS, nullable = false)
    private Transfer.TransactionStatus transactionStatus = Transfer.TransactionStatus.CREATED;

    @Column(name = RESPONSE, columnDefinition = "jsonb")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> response;

    @Column(name = RESPONSE_ASYNC, columnDefinition = "jsonb")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> responseAsync;

    @Column(name = DATE_CREATED, nullable = false)
    private Timestamp dateCreated;

    @Column(name = DATE_MODIFIED, nullable = false)
    private Timestamp dateModified;

    @Column(name = DATE_RESPONSE)
    private Timestamp dateResponse;

    @ManyToOne
    @JoinColumn(name = TRANSACTION)
    private Transaction transaction;

    public Source()
    {
    }

    public Timestamp getDateResponse()
    {
        return dateResponse;
    }

    public void setDateResponse(Timestamp dateResponse)
    {
        this.dateResponse = dateResponse;
    }

    public Transfer.SourceType getType()
    {
        return type;
    }

    public void setType(Transfer.SourceType type)
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

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
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

    public Transfer.TransactionStatus getTransactionStatus()
    {
        return transactionStatus;
    }

    public void setTransactionStatus(Transfer.TransactionStatus transactionStatus)
    {
        this.transactionStatus = transactionStatus;
    }

    public Map<String, Object> getResponse()
    {
        return response;
    }

    public void setResponse(Map<String, Object> response)
    {
        this.response = response;
    }

    public Map<String, Object> getResponseAsync()
    {
        return responseAsync;
    }

    public void setResponseAsync(Map<String, Object> responseAsync)
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
