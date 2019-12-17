package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.CardOrderInfo;
import net.tospay.transaction.models.DeviceInfo;
import net.tospay.transaction.models.MerchantInfo;
import net.tospay.transaction.models.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "user_id",
        "user_type",
        "account",
        "amount"
})
public class TransferOutgoingRequest extends BaseModel implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("merchantInfo")
    MerchantInfo merchantInfo;

    @JsonProperty("action")
    private String action;

    @JsonProperty("description")
    private String description;

    @JsonProperty("external_reference")
    private UUID externalReference;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Amount amount;

    //TODO:Fix later? fields for card
    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonProperty("orderInfo")
    private CardOrderInfo orderInfo;

    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public MerchantInfo getMerchantInfo()
    {
        return merchantInfo;
    }

    public void setMerchantInfo(MerchantInfo merchantInfo)
    {
        this.merchantInfo = merchantInfo;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }

    public CardOrderInfo getOrderInfo()
    {
        return orderInfo;
    }

    public void setOrderInfo(CardOrderInfo orderInfo)
    {
        this.orderInfo = orderInfo;
    }

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

    public UUID getExternalReference()
    {
        return externalReference;
    }

    public void setExternalReference(UUID externalReference)
    {
        this.externalReference = externalReference;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getAction()
    {
        return action;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    @JsonProperty("account")
    public Account getAccount()
    {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(Account account)
    {
        this.account = account;
    }

    public TransferOutgoingRequest withAccount(Account account)
    {
        this.account = account;
        return this;
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

    public TransferOutgoingRequest withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}