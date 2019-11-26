package net.tospay.transaction.models.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.response.MerchantInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sources",
        "delivery"
})
public class TransferRequest
{
    @JsonProperty("source")
    private List<TopupValueSource> source;

    @JsonProperty("delivery")
    private List<TopupValueDestination> delivery;

    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonProperty("deviceInfo")
    private DeviceInfo deviceInfo;

    @JsonProperty("merchantInfo")
    private MerchantInfo merchantInfo;

    @JsonProperty("account")
    private Account account;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("type")
    private Transfer.TransactionType type;

    @JsonProperty("external_reference")
    private String externalReference;

    public String getExternalReference()
    {
        return externalReference;
    }

    public void setExternalReference(String externalReference)
    {
        this.externalReference = externalReference;
    }

    public Transfer.TransactionType getType()
    {
        return type;
    }

    public void setType(Transfer.TransactionType type)
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

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
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

    public List<TopupValueDestination> getDelivery()
    {
        return delivery;
    }

    public void setDelivery(List<TopupValueDestination> delivery)
    {
        this.delivery = delivery;
    }

    public List<TopupValueSource> getSource()
    {
        return source;
    }

    public void setSource(List<TopupValueSource> source)
    {
        this.source = source;
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

    public TransferRequest withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}