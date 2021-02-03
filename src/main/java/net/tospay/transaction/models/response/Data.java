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

import net.tospay.transaction.models.MerchantInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userInfo"
})
public class Data implements Serializable
{
    private final static long serialVersionUID = -4311634145962810033L;

    @JsonProperty("userInfo")
    private UserInfo userInfo;

    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

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