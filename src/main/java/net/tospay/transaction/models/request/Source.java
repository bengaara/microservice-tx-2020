package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.models.Amount;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class Source implements Serializable
{
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("account")
    private Map<String, Object> account;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("id")
    private String id;

    @JsonProperty("channel")
    private AccountType channel;

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

    public Map<String, Object> getAccount()
    {
        return account;
    }

    public void setAccount(Map<String, Object> account)
    {
        this.account = account;
    }

    public Source withAccount(Map<String, Object> account)
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
    public AccountType getChannel()
    {
        return channel;
    }

    @JsonProperty("from")
    public void setChannel(AccountType channel)
    {
        this.channel = channel;
    }

    public Source withChannel(AccountType channel)
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