package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.StoreActionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransactionNode extends BaseModel {
   // static Logger logger = LoggerFactory.getLogger(TransactionNode.class);

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("sourceChannel")
    private AccountType sourceChannel;
    @JsonProperty("amount")
    private Number amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("charge")
    private Number charge;

    @JsonProperty("availableBalance")
    private Number availableBalance;
    @JsonProperty("user_id")
    private UUID userId;
    @JsonProperty("type")
    private UserType type;
    @JsonProperty("name")
    private String name;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("country")
    private String country;
    @JsonProperty("countryIso")
    private String countryIso;
    @JsonProperty("dateCreatedFormatted")
    private String dateCreatedFormatted;
    @JsonProperty("timeCreated")
    private String timeCreated;
    @JsonProperty("date_created")
    private LocalDateTime dateCreated;
    @JsonProperty("date_updated")
    private LocalDateTime dateUpdated;
    @JsonProperty("status")
    private String status;
    @JsonProperty("code")
    private String code;
    @JsonProperty("message")
    private String message;
    @JsonProperty("description")
    private String description;
    @JsonProperty("operation")
    private StoreActionType operation;//debit/credit

    static TransactionNode from(Source s) {
        TransactionNode res = new TransactionNode();

        res.setUserId(s.getPayload().getAccount().getUserId());
        res.setType(s.getPayload().getAccount().getUserType());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setAmount(s.getPayload().getTotal().getAmount());
        if (s.getPayload().getCharge() != null) {
            res.setCharge(s.getPayload().getCharge().getAmount());
        }
//        if(s.getPayload().getAccount().getCountry() !=null){
//            final List<String> timeZones = Stream.of(TimeZone.getAvailableIDs())
//                    .filter(zoneId -> zoneId.startsWith(s.getPayload().getAccount().getCountry().getIso())).collect(
//                            Collectors.toList());
//            String date =  Utils.FORMATTER.format(s.getDateCreated().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(timeZones.get(0))) .toLocalDateTime());
//            res.setDateCreatedFormatted(date);
//
//        }

        res.setDateCreated(s.getDateCreated());
        //res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated()));
        res.setDateUpdated(s.getDateModified());
        res.setDateCreatedFormatted(Utils.FORMATTER_DAY_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));
        res.setTimeCreated(Utils.FORMATTER_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));

        res.setId(s.getId());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setName(s.getPayload().getAccount().getName());
        res.setEmail(s.getPayload().getAccount().getEmail());
        res.setPhone(s.getPayload().getAccount().getPhone() != null ? s.getPayload().getAccount().getPhone() : "");
        if (s.getPayload().getAccount().getCountry() != null) {
            res.setCountry(s.getPayload().getAccount().getCountry().getName());
            res.setCountryIso(s.getPayload().getAccount().getCountry().getIso());
        }
        if (res.getEmail() == null) {
            // logger.debug("missing email,name country fields, populating from userinfo Transaction: {} {}", s.getTransaction().getId(), s.getTransaction().getPayload().getUserInfo().getTypeId());

            res.setName(s.getTransaction().getPayload().getUserInfo().getName());
            res.setEmail(s.getTransaction().getPayload().getUserInfo().getEmail());
            if (s.getTransaction().getPayload().getUserInfo().getCountry() != null) {
                res.setCountry(s.getTransaction().getPayload().getUserInfo().getCountry().getName());
                res.setCountryIso(s.getTransaction().getPayload().getUserInfo().getCountry().getIso());
            }
        }

        res.setCode(s.getCode());
        res.setMessage(s.getReason());


        res.setStatus(s.getTransactionStatus().name());
        res.setOperation(StoreActionType.DEBIT);

        res.setAvailableBalance(s.getAvailableBalance());

        return res;
    }

    public static TransactionNode from(Destination s) {
        TransactionNode res = new TransactionNode();

        res.setUserId(s.getPayload().getAccount().getUserId());
        res.setType(s.getPayload().getAccount().getUserType());
        res.setCurrency(s.getPayload().getTotal().getCurrency());
        res.setAmount(s.getPayload().getTotal().getAmount());
        if (s.getPayload().getCharge() != null) {
            res.setCharge(s.getPayload().getCharge().getAmount());
        }

        res.setDateCreated(s.getDateCreated());
        res.setDateUpdated(s.getDateModified());
        res.setDateCreatedFormatted(Utils.FORMATTER_DAY_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));
        res.setTimeCreated(Utils.FORMATTER_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));

        res.setId(s.getId());
        res.setSourceChannel(s.getPayload().getAccount().getType());
        res.setName(s.getPayload().getAccount().getName() != null ? s.getPayload().getAccount().getName() : "");
        res.setEmail(s.getPayload().getAccount().getEmail() != null ? s.getPayload().getAccount().getEmail() : "");
        res.setPhone(s.getPayload().getAccount().getPhone() != null ? s.getPayload().getAccount().getPhone() : "");
        if (s.getPayload().getAccount().getCountry() != null) {
            res.setCountry(s.getPayload().getAccount().getCountry().getName());
            res.setCountryIso(s.getPayload().getAccount().getCountry().getIso());
        }

        res.setCode(s.getCode());
        res.setMessage(s.getReason());


        res.setStatus(s.getTransactionStatus().name());
        res.setOperation(StoreActionType.CREDIT);
        // res.setDescription();

        res.setAvailableBalance(s.getAvailableBalance());

        return res;
    }


    static TransactionNode from(Store s) {
        TransactionNode res = new TransactionNode();

        res.setUserId(s.getAccount().getUserId());
        res.setType(s.getAccount().getUserType());
        res.setCurrency(s.getTotal().getCurrency());
        res.setAmount(s.getTotal().getAmount());
        if (s.getCharge() != null) {
            res.setCharge(s.getCharge().getAmount());
        }
//        if(s.getPayload().getAccount().getCountry() !=null){
//            final List<String> timeZones = Stream.of(TimeZone.getAvailableIDs())
//                    .filter(zoneId -> zoneId.startsWith(s.getPayload().getAccount().getCountry().getIso())).collect(
//                            Collectors.toList());
//            String date =  Utils.FORMATTER.format(s.getDateCreated().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of(timeZones.get(0))) .toLocalDateTime());
//            res.setDateCreatedFormatted(date);
//
//        }

      //  res.setDateCreated(s.getDateCreated());
        //res.setDateCreatedFormatted(Utils.FORMATTER.format(s.getDateCreated()));
     //   res.setDateUpdated(s.getDateModified());
//        res.setDateCreatedFormatted(Utils.FORMATTER_DAY_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
//            .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));
//        res.setTimeCreated(Utils.FORMATTER_TIME.format(s.getDateCreated().atZone(ZoneId.systemDefault())
//            .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));
//
//        res.setId(s.getId());
        res.setSourceChannel(s.getAccount().getType());
        res.setName(s.getAccount().getName());
        res.setEmail(s.getAccount().getEmail());
        res.setPhone(s.getAccount().getPhone() != null ? s.getAccount().getPhone() : "");
        if (s.getAccount().getCountry() != null) {
            res.setCountry(s.getAccount().getCountry().getName());
            res.setCountryIso(s.getAccount().getCountry().getIso());
        }

//        res.setCode(s.getCode());
//        res.setMessage(s.getReason());
//        res.setStatus(s.getTransactionStatus().name());
     //   res.setOperation(StoreActionType.DEBIT);


        return res;
    }

}
