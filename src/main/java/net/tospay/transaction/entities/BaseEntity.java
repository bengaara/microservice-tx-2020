package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vladmihalcea.hibernate.type.array.IntArrayType;
import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonNodeStringType;
import com.vladmihalcea.hibernate.type.json.JsonStringType;
import java.io.Serializable;
import java.util.Arrays;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import lombok.Data;
import net.tospay.transaction.util.Utils;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "int-array", typeClass = IntArrayType.class),
        @TypeDef(name = "json", typeClass = JsonStringType.class),
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class),
        @TypeDef(name = "jsonb-node", typeClass = JsonNodeBinaryType.class),
        @TypeDef(name = "json-node", typeClass = JsonNodeStringType.class)})
@MappedSuperclass
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(generator= ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
public abstract class BaseEntity<T> implements Serializable {

    //    public static final String DATE_CREATED = "date_created";
//    private static final String DATE_MODIFIED = "date_modified";
//    private static final String ID = "id";

    @JsonIgnore
    @JsonBackReference
    @Transient
    boolean hideData = false;
//    @Id
//    @Column(name = ID, columnDefinition = "uuid default gen_random_uuid()", updatable = false)
//    @GeneratedValue
//    @Type(type = "org.hibernate.type.PostgresUUIDType")
//    private UUID id;
//    @Column(name = DATE_CREATED, nullable = false)
//    private LocalDateTime dateCreated;
//    @Column(name = DATE_MODIFIED)
//    private LocalDateTime dateModified;

    public static String toDbField(String s) {
        return Arrays.asList(s.split("_")).stream()
                .map(String::toLowerCase)
                .reduce((s1, s2) -> s1 + s2.substring(0, 1).toUpperCase() + s2.substring(1).toLowerCase()).get();

    }

    //   public abstract T getId();
    //   public abstract void setId(T id);

    public boolean isHideData() {
        return hideData;

    }

    public BaseEntity setHideData(boolean hideData) {
        this.hideData = hideData;
        LoggerFactory.getLogger(this.getClass()).debug("setHideData {} {}  ", hideData, Utils.getCallerCallerClassName());
        return this;
    }

    @Override
    public String toString() {
        String s = "";
        if (!hideData) {
            s = Utils.inspect(this);
        } else {
            s = super.toString();
        }
        hideData = !hideData;//toggle show once only
        LoggerFactory.getLogger(this.getClass()).debug("toString toggling hideData to {} {}", hideData, Utils.getCallerCallerClassName());
        return s;
    }

//    @PreUpdate
//    protected void preUpdate() {
//        dateModified = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
//    }
//
//    @PrePersist
//    protected void prePersist() {
//        dateCreated = LocalDateTime.now();//new Timestamp(System.currentTimeMillis());
//    }
}
