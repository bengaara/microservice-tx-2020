package net.tospay.transaction.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.UserInfo;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@JsonIgnoreProperties
@Data
public class SourceDestinationMerge {
    @Column(name = "source")
    Source source;

    @Column(name = "destination")
    Destination destination;

    @Column(name = "user_info")
    UserInfo userInfo;

}
