package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Data;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.Store;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "destinations",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"id"}))
@JsonIgnoreProperties
@Data
public class Destination extends BaseSource {

}
