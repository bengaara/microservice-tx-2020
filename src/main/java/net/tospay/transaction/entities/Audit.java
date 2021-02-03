package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.tospay.transaction.models.BaseModel;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Audits",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
public class Audit extends BaseEntity<UUID>{
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

    @Column(name = "request", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private BaseModel request;
    @Column(name = "response", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private BaseModel response;
    @Column(name = "service", nullable = false)
    private String service;
    @Column(name = "operation", nullable = false)
    private String operation;
    @Column(name = "executed_by", nullable = false)
    private UUID executedBy;
    @Column(name = "record_changes", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    private BaseModel recordChanges;
    @Column(name = "notes", nullable = false)
    private String notes;


}
