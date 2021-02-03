package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.request.OrderInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TransactionRequest extends BaseModel {


    @JsonProperty("fraudInfo")
    FraudInfo fraudInfo;
    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;
    @JsonProperty("type")
    private TransactionType type;
    @JsonProperty("source")
    private List<Store> source;
    @JsonProperty("delivery")
    private List<Store> delivery;
    @JsonProperty("userInfo")
    private UserInfo userInfo;
    @JsonProperty("orderInfo")
    private OrderInfo orderInfo;
    @JsonProperty("chargeInfo")
    private ChargeInfo chargeInfo;

    //special for logs from pos
    @JsonProperty("status")
    private TransactionStatus status;

    @JsonProperty("sumSourceAmount")
    private Amount sumSourceAmount;


    public TransactionRequest() {
        // logger.debug("new TransactionRequest caller: {}", Utils.getCallerCallerClassName());
        //setHideData(false);
    }
}