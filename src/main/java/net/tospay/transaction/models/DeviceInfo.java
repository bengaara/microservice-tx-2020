package net.tospay.transaction.models;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceInfo extends BaseModel
{
    @NotNull
    @JsonProperty("ip")
    private List<String> ip;

    @NotNull
    @JsonProperty("userAgent")
    private String userAgent;

    public List<String> getIp()
    {
        return ip;
    }

    public void setIp(List<String> ip)
    {
        this.ip = ip;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }
}