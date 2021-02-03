package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.StoreActionType;
import net.tospay.transaction.enums.TransactionStatus;
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
public class TransactionFetchResponse extends BaseModel {

    static Logger logger = LoggerFactory.getLogger(TransactionFetchResponse.class);
    // @formatter:off

    @JsonIgnore
    Transaction transaction;

    @JsonProperty("source")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> source = new ArrayList<>();
    @JsonProperty("destination")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<TransactionNode> destination = new ArrayList<>();

    @JsonProperty("reversed")
    Boolean reversed;
    @JsonProperty("reversalChild")
    TransactionFetchResponse reversalChild;
    @JsonProperty("reversalParent")
    TransactionFetchResponse reversalParent;
    @JsonProperty("transactionId")
    private String transactionId;//friendly name
    //transaction.id
    @JsonProperty("id")
    private UUID id;
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
    @JsonProperty("user_id")
    private UUID userId;
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
    @JsonProperty("dateCreatedFormattedShort")
    private String dateCreatedFormattedShort;
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


    @JsonProperty("sub_type")
    private OrderType subType;

    @JsonProperty("utility")
    private BaseModel utility;

    // @formatter:on
    public static TransactionFetchResponse from(Transaction transaction) {
        TransactionFetchResponse res = new TransactionFetchResponse();
        res.transaction=transaction;
        res.setId(transaction.getId());
        res.setUserId(transaction.getUserInfo().getUserId());
        res.setUserType(transaction.getUserInfo().getTypeId());
        res.setTransactionId(transaction.getTransactionId());
        if (transaction.getPayload().getOrderInfo() != null) {
            res.setReference(transaction.getPayload().getOrderInfo().getReference());
            res.setCurrency(transaction.getPayload().getOrderInfo().getAmount().getCurrency());
            res.setAmount(transaction.getPayload().getOrderInfo().getAmount().getAmount());
            res.setUtility(transaction.getPayload().getOrderInfo().getUtility());
        }
        res.setStatus(transaction.getTransactionStatus());

        if (transaction.getReversalChild() != null && transaction.getReversalParent() == null) {
            logger.debug("is reversed: true {}", transaction.getId());
            res.setReversalChild(TransactionFetchResponse.from(transaction.getReversalChild()));
            res.setReversed(true);
        }
//        if (transaction.getReversalParent() != null) {
//            logger.debug("is reversal: true {}", transaction.getId());
//            res.setReversalParent(TransactionFetchResponse.from(transaction.getReversalParent()));
//        }

        res.setCode(transaction.getCode());
        res.setReason(transaction.getReason());
        res.setDescription(transaction.getPayload().getOrderInfo().getDescription());


        if (transaction.getPayload().getChargeInfo() != null) {
            BigDecimal charge = transaction.getPayload().getChargeInfo().getSource().getAmount();
            charge = charge
                .add(transaction.getPayload().getChargeInfo().getDestination().getAmount());
            res.setCharge(charge);

           if(transaction.getPayload().getChargeInfo().getRailInfo() !=null && transaction.getPayload().getChargeInfo().getRailInfo().getAmount() !=null){
               res.setRailRevenue(transaction.getPayload().getChargeInfo().getRailInfo().getAmount().getAmount());
           }
            if(transaction.getPayload().getChargeInfo().getPartnerInfo() !=null && transaction.getPayload().getChargeInfo().getPartnerInfo().getAmount() !=null){
                res.setPartnerRevenue(transaction.getPayload().getChargeInfo().getPartnerInfo().getAmount().getAmount());
            }

        }
        res.setType(transaction.getType());
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


        transaction.getSources().stream().forEach(source -> {
            TransactionNode transactionNode = TransactionNode.from(source);
            res.getSource().add(transactionNode);
        });
        transaction.getDestinations().stream().forEach(destination -> {
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
            dateCreatedFormattedShort = Utils.FORMATTER_DAY_MINI.format(dateCreated.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime());
            timeCreated = Utils.FORMATTER_TIME.format(dateCreated.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime());
        }
    }
    static String getAction(UUID statementOwnerId,Transaction transaction ) {

        //skip if source n dest match
        StoreActionType storeActionType = StoreActionType.CREDIT;

        if (transaction.getUserInfo().getUserId().equals(statementOwnerId)) {
            storeActionType = StoreActionType.DEBIT;
        }


        switch (transaction.getType()) {

            //    break;
            case TOPUP:
            case TRANSFER:
            case PAYMENT:
            case SETTLEMENT:
            case REVERSAL:
                return StoreActionType.DEBIT.equals(storeActionType) ? "Sent" : "Received";
            case WITHDRAWAL:
                return StoreActionType.DEBIT.equals(storeActionType) ? "Withdrawn" : "Received";
            case UTILITY:
                if (transaction.getPayload().getOrderInfo().getUtility() != null) {

                    String action = String.valueOf(transaction.getPayload().getOrderInfo().getUtility().getAdditionalProperties()
                            .get("action")); //BUY_AIRTIME,BUY_BUNDLE,UNSUBSCRIBE_BUNDLE
                    action = action.replace("_", " ").replace("BUY_", "")
                        .replace("UNSUBSCRIBE_", "").toLowerCase();

                   return action;
                }

            default:
                logger.debug("default getAction {} {} ",storeActionType, transaction.getId());
                return StoreActionType.DEBIT.equals(storeActionType) ? "Sent" : "Received";
        }

    }

}
