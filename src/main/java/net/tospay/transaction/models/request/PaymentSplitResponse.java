package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.models.response.MerchantInfo;
import net.tospay.transaction.models.response.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentSplitResponse
{
    @JsonProperty("merchantInfo")  MerchantInfo merchantInfo;

    @JsonProperty("orderInfo") OrderInfo orderInfo;

    @JsonProperty("splitInfo")  SplitInfo splitInfo;

    @JsonProperty("userInfo")  UserInfo userInfo;

    public MerchantInfo getMerchantInfo()
    {
        return merchantInfo;
    }

    public void setMerchantInfo(MerchantInfo merchantInfo)
    {
        this.merchantInfo = merchantInfo;
    }

    public OrderInfo getOrderInfo()
    {
        return orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo)
    {
        this.orderInfo = orderInfo;
    }

    public SplitInfo getSplitInfo()
    {
        return splitInfo;
    }

    public void setSplitInfo(SplitInfo splitInfo)
    {
        this.splitInfo = splitInfo;
    }

    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }
}