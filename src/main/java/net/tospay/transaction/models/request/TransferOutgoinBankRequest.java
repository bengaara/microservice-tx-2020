package net.tospay.transaction.models.request;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.DeviceInfo;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.MerchantInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sources",
        "delivery"
})
public class TransferOutgoinBankRequest  extends BaseModel
{
    @JsonProperty("orderInfo")
    private TransferOutgoingRequest orderInfo;

    public TransferOutgoingRequest getOrderInfo()
    {
        return orderInfo;
    }

    public void setOrderInfo(TransferOutgoingRequest orderInfo)
    {
        this.orderInfo = orderInfo;
    }

    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;

    @JsonProperty("merchantInfo")
    private MerchantInfo merchantInfo;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Number amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("type")
    private TransactionType type;

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

    public MerchantInfo getMerchantInfo()
    {
        return merchantInfo;
    }

    public void setMerchantInfo(MerchantInfo merchantInfo)
    {
        this.merchantInfo = merchantInfo;
    }

    public Account getAccount()
    {
        return account;
    }

    public void setAccount(Account account)
    {
        this.account = account;
    }

    public Number getAmount()
    {
        return amount;
    }

    public void setAmount(Number amount)
    {
        this.amount = amount;
    }

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }


    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties)
    {
        this.additionalProperties = additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public TransferOutgoinBankRequest withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}