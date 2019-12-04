package net.tospay.transaction.models;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceInfo
{
        @NotNull
        @JsonProperty("ip")
        private String ip;
        @NotNull
        @JsonProperty("userAgent")
        private String userAgent;

    public String getIp()
    {
        return ip;
    }

    public void setIp(String ip)
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