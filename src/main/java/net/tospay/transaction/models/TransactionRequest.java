package net.tospay.transaction.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.request.OrderInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sources",
        "delivery"
})
public class TransactionRequest
{
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

    public DeviceInfo getDeviceInfo()
    {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo)
    {
        this.deviceInfo = deviceInfo;
    }

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public List<Store> getSource()
    {
        return source;
    }

    public void setSource(List<Store> source)
    {
        this.source = source;
    }

    public List<Store> getDelivery()
    {
        return delivery;
    }

    public void setDelivery(List<Store> delivery)
    {
        this.delivery = delivery;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }

    public OrderInfo getOrderInfo()
    {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo)
    {
        this.orderInfo = orderInfo;
    }

    public ChargeInfo getChargeInfo()
    {
        return chargeInfo;
    }

    public void setChargeInfo(ChargeInfo chargeInfo)
    {
        this.chargeInfo = chargeInfo;
    }
}