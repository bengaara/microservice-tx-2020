package net.tospay.transaction.models.response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userInfo",
        "merchantInfo"
})
public class Data implements Serializable
{
    private final static long serialVersionUID = -4311634145962810033L;

    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonProperty("merchantInfo")
    private MerchantInfo merchantInfo;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("userInfo")
    public UserInfo getUserInfo()
    {
        return userInfo;
    }

    @JsonProperty("userInfo")
    public void setUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
    }

    public Data withUserInfo(UserInfo userInfo)
    {
        this.userInfo = userInfo;
        return this;
    }

    @JsonProperty("merchantInfo")
    public MerchantInfo getMerchantInfo()
    {
        return merchantInfo;
    }

    @JsonProperty("merchantInfo")
    public void setMerchantInfo(MerchantInfo merchantInfo)
    {
        this.merchantInfo = merchantInfo;
    }

    public Data withMerchantInfo(MerchantInfo merchantInfo)
    {
        this.merchantInfo = merchantInfo;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public Data withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}