package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.entities.TransactionInit;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonInclude(JsonInclude.Include.NON_NULL)//Include.NON_EMPTY
//@JsonPropertyOrder({
//        "transactionId",
//})
@Data
public class TransactionInitFetchResponse extends BaseModel {

    static Logger logger = LoggerFactory.getLogger(TransactionInitFetchResponse.class);
    // @formatter:off
    @JsonProperty("source")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> source = new ArrayList<>();
    @JsonProperty("destination")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> destination = new ArrayList<>();

    //transaction.id
    @JsonProperty("id")
    private UUID id;
    @JsonProperty("reference")
    private String reference;
    @JsonProperty("description")
    private String description;
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
    @JsonProperty("availableBalance")
    private Number availableBalance;
    @JsonProperty("charge")
    private Number charge;

    @JsonProperty("partnerRevenue")
    private Number partnerRevenue;
    @JsonProperty("railRevenue")
    private Number railRevenue;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("agent_id")
    private UUID agentId;
    @JsonProperty("user_type")
    private UserType userType;
    @JsonProperty("name")
    private String name;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
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
    private MakerCheckerStatus status;
    @JsonProperty("code")
    private String code;
    @JsonProperty("reason")
    private String reason;


    @JsonProperty("sub_type")
    private OrderType subType;

    @JsonProperty("utility")
    private BaseModel utility;

    @JsonProperty("remarks")
    private String remarks;

    // @formatter:on
    public static TransactionInitFetchResponse from(TransactionInit transaction) {
        TransactionInitFetchResponse res = new TransactionInitFetchResponse();
        res.setId(transaction.getId());
        res.setUserId(transaction.getUserInfo().getUserId());
        res.setAgentId(transaction.getUserInfo().getAgentId());
        res.setUserType(transaction.getUserInfo().getTypeId());

        if (transaction.getPayload().getOrderInfo() != null) {
            res.setReference(transaction.getPayload().getOrderInfo().getReference());
            res.setDescription(transaction.getPayload().getOrderInfo().getDescription());
            res.setCurrency(transaction.getPayload().getOrderInfo().getAmount().getCurrency());
            res.setAmount(transaction.getPayload().getOrderInfo().getAmount().getAmount());
            res.setUtility(transaction.getPayload().getOrderInfo().getUtility());
        }
        res.setStatus(transaction.getMCStatus());
        long l = transaction.getCheckerRecords().stream().count();
        if (l > 0) {
            res.setRemarks(
                transaction.getCheckerRecords().stream().skip(l - 1).findFirst().get()
                    .getRemarks());
        }
//        if (transaction.getReversalParent() != null) {
//            logger.debug("is reversal: true {}", transaction.getId());
//            res.setReversalParent(TransactionFetchResponse.from(transaction.getReversalParent()));
//        }

        //   res.setCode(transaction.getCode());
        //   res.setReason(transaction.getReason());

        if (transaction.getPayload().getChargeInfo() != null) {
            BigDecimal charge = transaction.getPayload().getChargeInfo().getSource().getAmount();
            charge = charge
                .add(transaction.getPayload().getChargeInfo().getDestination().getAmount());
            res.setCharge(charge);
            res.setRailRevenue(transaction.getPayload().getChargeInfo().getRailInfo() == null ? null
                : transaction.getPayload().getChargeInfo().getRailInfo().getAmount().getAmount());
            res.setPartnerRevenue(
                transaction.getPayload().getChargeInfo().getPartnerInfo() == null ? null
                    : transaction.getPayload().getChargeInfo().getPartnerInfo().getAmount()
                        .getAmount());
        }
        res.setType(transaction.getPayload().getType());
        if (transaction.getPayload().getOrderInfo() != null) {
            res.setSubType(transaction.getPayload().getOrderInfo().getType());
        }

        if (transaction.getPayload().getUserInfo() != null) {
            res.setUserId(transaction.getPayload().getUserInfo().getUserId());
            res.setUserType(transaction.getUserInfo().getTypeId());
            res.setName(transaction.getPayload().getUserInfo().getName());
            res.setEmail(transaction.getPayload().getUserInfo().getEmail());
            res.setPhone(transaction.getPayload().getUserInfo().getPhone());
            if (transaction.getPayload().getUserInfo().getCountry() != null) {
                res.setCountry(transaction.getPayload().getUserInfo().getCountry().getName());
                res.setCountryIso(transaction.getPayload().getUserInfo().getCountry().getIso());
            }
        }


        res.setDateCreated(transaction.getDateCreated());
        // res.setDateCreated(transaction.getDateModified());
        //res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated()));
        res.setDateUpdated(transaction.getDateModified());


        transaction.getPayload().getSource().stream().forEach(source -> {
            TransactionNode transactionNode = TransactionNode.from(source);

            res.getSource().add(transactionNode);
        });
        transaction.getPayload().getDelivery().stream().forEach(destination -> {
            TransactionNode transactionNode = TransactionNode.from(destination);

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
