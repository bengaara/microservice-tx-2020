package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransactionFetchRequest extends BaseModel {

    @JsonProperty("transaction_id")
    String transactionId;

    @JsonProperty("reference")
    String reference;

    @JsonProperty("user_id")
    UUID userId;

    @JsonProperty("msisdn")
    String msisdn;
    //transaction.id
    @JsonProperty("id")
    UUID id;
    @JsonProperty("userInfo")
    UserInfo userInfo;
    //@JsonInclude(value= JsonInclude.Include.NON_EMPTY)
    //  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MMM-yyyy", timezone="PST")
    @JsonProperty("from")
    LocalDate from;
    @JsonProperty("to")
    LocalDate to;
    String type; //MINI/FULL
    @JsonProperty("offset")
    private Integer offset = 0;
    @JsonProperty("limit")
    private Integer limit ;

    public void setId(String someUUID){
        try{
            id = UUID.fromString(someUUID);
            //do something
        } catch (IllegalArgumentException e){
            e.getStackTrace();
        }
    }
    public void setUserId(String someUUID){
        try{
            userId = UUID.fromString(someUUID);
            //do something
        } catch (IllegalArgumentException e){
            e.getStackTrace();
        }
    }

    public LocalDate getTo() {
        if (to != null) {
            return to.plusDays(1).atStartOfDay().toLocalDate();
        }
        return to;
    }
}