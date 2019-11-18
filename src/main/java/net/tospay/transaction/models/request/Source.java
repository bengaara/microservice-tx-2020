package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.SourceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "account",
        "platform",
        "id",
        "channel"
})
public class Source implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("account")
    private String account;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("id")
    private String id;

    @JsonProperty("channel")
    private SourceType channel;

    @JsonProperty("amount")
    private Amount amount;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Amount getAmount()
    {
        return amount;
    }

    public void setAmount(Amount amount)
    {
        this.amount = amount;
    }

    public String getPlatform()
    {
        return platform;
    }

    public void setPlatform(String platform)
    {
        this.platform = platform;
    }

    @JsonProperty("account")
    public String getAccount()
    {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account)
    {
        this.account = account;
    }

    public Source withAccount(String account)
    {
        this.account = account;
        return this;
    }

    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public Source withId(String id)
    {
        this.id = id;
        return this;
    }

    @JsonProperty("from")
    public SourceType getChannel()
    {
        return channel;
    }

    @JsonProperty("from")
    public void setChannel(SourceType channel)
    {
        this.channel = channel;
    }

    public Source withChannel(SourceType channel)
    {
        this.channel = channel;
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

    public Source withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}