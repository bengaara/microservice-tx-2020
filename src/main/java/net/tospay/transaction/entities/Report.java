package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "transactions_reports",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class  Report extends BaseEntity<UUID> {

    public static final String DATE_CREATED = "date_created";
    private static final String DATE_MODIFIED = "date_modified";
    private static final String ID = "id";

    @Id
    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
    @GeneratedValue
    @Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    @Column(name = DATE_CREATED, nullable = false)
    private LocalDateTime dateCreated;
    @Column(name = DATE_MODIFIED)
    private LocalDateTime dateModified;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email")
    private String email;

    @Column(name = "report_number")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_number_gen")
    @SequenceGenerator(name = "report_number_gen", sequenceName = "report_number_gen", allocationSize = 1)
    //TODO: remember this query create sequence if not exists report_number_gen increment 1;
    private Integer reportNumber;

//    @Column(name = "payload", columnDefinition = "jsonb")
//    @Type(type = "jsonb")
//    private MT940 payload;

    @Column(name = "MT940_payload_string", columnDefinition = "TEXT")
    private String MT940PayloadString;

    @Column(name = "transaction_count")
    private Number transactionCount;


    @Column(name = "date_from", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDateTime dateTo;

    @Column(name = "opening_balance", nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", nullable = false)
    private BigDecimal closingBalance;

    @Column(name = "sent")
    private boolean sent;

    @PreUpdate
    protected void preUpdate() {
        dateModified = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }

    @PrePersist
    protected void prePersist() {
        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
    }
}
