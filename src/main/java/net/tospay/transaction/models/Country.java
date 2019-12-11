package net.tospay.transaction.models;

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

public class Country implements Serializable
{
    private final static long serialVersionUID = 7387601059382747089L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("iso")
    private String iso;

    @JsonProperty("name")
    private String name;

    @JsonProperty("nicename")
    private String nicename;

    @JsonProperty("iso3")
    private String iso3;

    @JsonProperty("numcode")
    private String numcode;

    @JsonProperty("phonecode")
    private String phonecode;

    @JsonProperty("currency")
    private String currency;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    public Country withId(String id)
    {
        this.id = id;
        return this;
    }

    @JsonProperty("iso")
    public String getIso()
    {
        return iso;
    }

    @JsonProperty("iso")
    public void setIso(String iso)
    {
        this.iso = iso;
    }

    public Country withIso(String iso)
    {
        this.iso = iso;
        return this;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Country withName(String name)
    {
        this.name = name;
        return this;
    }

    @JsonProperty("nicename")
    public String getNicename()
    {
        return nicename;
    }

    @JsonProperty("nicename")
    public void setNicename(String nicename)
    {
        this.nicename = nicename;
    }

    public Country withNicename(String nicename)
    {
        this.nicename = nicename;
        return this;
    }

    @JsonProperty("iso3")
    public String getIso3()
    {
        return iso3;
    }

    @JsonProperty("iso3")
    public void setIso3(String iso3)
    {
        this.iso3 = iso3;
    }

    public Country withIso3(String iso3)
    {
        this.iso3 = iso3;
        return this;
    }

    @JsonProperty("numcode")
    public String getNumcode()
    {
        return numcode;
    }

    @JsonProperty("numcode")
    public void setNumcode(String numcode)
    {
        this.numcode = numcode;
    }

    public Country withNumcode(String numcode)
    {
        this.numcode = numcode;
        return this;
    }

    @JsonProperty("phonecode")
    public String getPhonecode()
    {
        return phonecode;
    }

    @JsonProperty("phonecode")
    public void setPhonecode(String phonecode)
    {
        this.phonecode = phonecode;
    }

    public Country withPhonecode(String phonecode)
    {
        this.phonecode = phonecode;
        return this;
    }

    @JsonProperty("currency")
    public String getCurrency()
    {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency)
    {
        this.currency = currency;
    }

    public Country withCurrency(String currency)
    {
        this.currency = currency;
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

    public Country withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}