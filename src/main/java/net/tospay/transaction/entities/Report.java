package net.tospay.transaction.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "reports",
        uniqueConstraints =
        @UniqueConstraint(columnNames = { "id" }))
@JsonIgnoreProperties
public class Report extends BaseEntity<UUID> implements Serializable
{
    private static final String DATE_MODIFIED = "date_modified";

    private static final String ID = "id";

    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "report_number")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_number_gen")
    @SequenceGenerator(name = "report_number_gen", sequenceName = "report_number_gen", allocationSize = 1)
    //TODO: remember this query create sequence if not exists report_number_gen increment 1;
    private Long reportNumber;

//    @Column(name = "payload", columnDefinition = "jsonb")
//    @Type(type = "jsonb")
//    private MT940 payload;

    @Column(name = "MT940_payload_string")
    private String MT940PayloadString;

    @Column(name = "transaction_count")
    private Number transactionCount;

    @Column(name = "NBK_payload_string")
    private String NBKPayloadString;

    @Column(name = "date_created", nullable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_from", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDateTime dateTo;

    @Column(name = "date_modified", nullable = false)
    private LocalDateTime dateModified;

    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false)
    private BigDecimal closingBalance;

    @Column(name = "sent")
    private boolean sent;

    public Report()
    {
    }

    public static String getDateModified()
    {
        return DATE_MODIFIED;
    }

    public void setDateModified(LocalDateTime dateModified)
    {
        this.dateModified = dateModified;
    }

    public static String getID()
    {
        return ID;
    }

    public Number getTransactionCount()
    {
        return transactionCount;
    }

    public void setTransactionCount(Number transactionCount)
    {
        this.transactionCount = transactionCount;
    }

    public String getMT940PayloadString()
    {
        return MT940PayloadString;
    }

    public void setMT940PayloadString(String MT940PayloadString)
    {
        this.MT940PayloadString = MT940PayloadString;
    }

    public String getNBKPayloadString()
    {
        return NBKPayloadString;
    }

    public void setNBKPayloadString(String NBKPayloadString)
    {
        this.NBKPayloadString = NBKPayloadString;
    }

    public BigDecimal getOpeningBalance()
    {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance)
    {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getClosingBalance()
    {
        return closingBalance;
    }

    public void setClosingBalance(BigDecimal closingBalance)
    {
        this.closingBalance = closingBalance;
    }

    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public Long getReportNumber()
    {
        return reportNumber;
    }

    public void setReportNumber(Long reportNumber)
    {
        this.reportNumber = reportNumber;
    }

    public boolean isSent()
    {
        return sent;
    }

    public void setSent(boolean sent)
    {
        this.sent = sent;
    }

    public LocalDateTime getDateCreated()
    {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated)
    {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateFrom()
    {
        return dateFrom;
    }

    public void setDateFrom(LocalDateTime dateFrom)
    {
        this.dateFrom = dateFrom;
    }

    public LocalDateTime getDateTo()
    {
        return dateTo;
    }

    public void setDateTo(LocalDateTime dateTo)
    {
        this.dateTo = dateTo;
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
        dateModified = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void prePersist()
    {
        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
        dateModified = dateCreated;
    }
}
