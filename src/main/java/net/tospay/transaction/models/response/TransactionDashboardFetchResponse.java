package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.ChargeInfo;
import net.tospay.transaction.models.DeviceInfo;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.request.OrderInfo;
import net.tospay.transaction.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)//Include.NON_EMPTY
@Data
public class TransactionDashboardFetchResponse extends BaseModel {
    //static Logger logger = LoggerFactory.getLogger(TransactionDashboardFetchResponse.class);
    @JsonProperty("source")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> source = new ArrayList<>();
    @JsonProperty("destination")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> destination = new ArrayList<>();
    @JsonProperty("transactionId")
    private String transactionId;//friendly name
    //transaction.id
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("reference")
    private String reference;
    @JsonProperty("type")
    private TransactionType type;
    @JsonProperty("orderType")
    private OrderType orderType;//qr split etc..
    @JsonProperty("sourceChannel")
    private AccountType sourceChannel;
    @JsonProperty("amount")
    private Number amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("charge")
    private Number charge;
    @JsonProperty("partnerRevenue")
    private Number partnerRevenue;
    @JsonProperty("railRevenue")
    private Number railRevenue;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("countryIso")
    private String countryIso;
    @JsonProperty("country")
    private String country;
    @JsonProperty("dateCreatedFormatted")
    private String dateCreatedFormatted;
    @JsonProperty("timeCreated")
    private String timeCreated;
    @JsonProperty("date_created")
    private LocalDateTime dateCreated;
    @JsonProperty("date_updated")
    private LocalDateTime dateUpdated;
    @JsonProperty("status")
    private TransactionStatus status;
    @JsonProperty("code")
    private String code;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("description")
    private String description;
    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;
    @JsonProperty("userInfo")
    private net.tospay.transaction.models.UserInfo userInfo;
    @JsonProperty("orderInfo")
    private OrderInfo orderInfo;
    @JsonProperty("chargeInfo")
    private ChargeInfo chargeInfo;
    @JsonProperty("availableBalance")
    private Number availableBalance;
    @JsonProperty("sub_type")
    private OrderType subType;

    public static TransactionDashboardFetchResponse from(Transaction transaction) {
        TransactionDashboardFetchResponse res = new TransactionDashboardFetchResponse();
        res.setId(transaction.getId());
        res.setUserId(transaction.getUserInfo().getUserId());
        res.setTransactionId(transaction.getTransactionId());
        res.setReference(transaction.getPayload().getOrderInfo().getReference());
        res.setStatus(transaction.getTransactionStatus());
        res.setCode(transaction.getCode());
        res.setReason(transaction.getReason());

        res.setDeviceInfo(transaction.getPayload().getDeviceInfo());
        res.setUserInfo(transaction.getPayload().getUserInfo());
        res.setChargeInfo(transaction.getPayload().getChargeInfo());
        res.setOrderInfo(transaction.getPayload().getOrderInfo());

        res.setCurrency(transaction.getPayload().getOrderInfo().getAmount().getCurrency());
        res.setAmount(transaction.getPayload().getOrderInfo().getAmount().getAmount());
        BigDecimal charge = transaction.getPayload().getChargeInfo().getSource().getAmount();
        charge = charge.add(transaction.getPayload().getChargeInfo().getDestination().getAmount());
        res.setCharge(charge);
        res.setRailRevenue(transaction.getPayload().getChargeInfo().getRailInfo() == null ? null : transaction.getPayload().getChargeInfo().getRailInfo().getAmount().getAmount());
        res.setPartnerRevenue(transaction.getPayload().getChargeInfo().getPartnerInfo() == null ? null : transaction.getPayload().getChargeInfo().getPartnerInfo().getAmount().getAmount());


        res.setType(transaction.getPayload().getType());
        res.setSubType(transaction.getPayload().getOrderInfo().getType());
        res.setName(transaction.getPayload().getUserInfo().getName());
        res.setEmail(transaction.getPayload().getUserInfo().getEmail());
        if (transaction.getPayload().getUserInfo().getCountry() != null) {
            res.setCountry(transaction.getPayload().getUserInfo().getCountry().getName());
            res.setCountryIso(transaction.getPayload().getUserInfo().getCountry().getIso());
        }


        res.setDateCreated(transaction.getDateCreated());
        //res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated()));
        res.setDateUpdated(transaction.getDateModified());

        transaction.getSources().stream().forEach(source -> {
            TransactionNode transactionNode = TransactionNode.from(source);
            if (source.getAvailableBalance() != null) {
                res.setAvailableBalance(source.getAvailableBalance());
            }
            res.getSource().add(transactionNode);
        });
        transaction.getDestinations().stream().forEach(destination -> {
            TransactionNode transactionNode = TransactionNode.from(destination);
            if (destination.getAvailableBalance() != null) {
                res.setAvailableBalance(destination.getAvailableBalance());
            }
            res.getDestination().add(transactionNode);
        });

        return res;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
        if (dateCreated != null) {
            dateCreatedFormatted = Utils.FORMATTER_DAY_TIME.format(dateCreated.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime());
            timeCreated = Utils.FORMATTER_TIME.format(dateCreated.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime());
        }

    }


}
