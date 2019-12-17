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
@JsonPropertyOrder({
        "city",
        "state",
        "postal_address",
        "postal_code"
})
public class Address extends BaseModel
{
    private final static long serialVersionUID = -5445788562091130288L;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("postal_address")
    private String postalAddress;

    @JsonProperty("postal_code")
    private String postalCode;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("city")
    public String getCity()
    {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city)
    {
        this.city = city;
    }

    public Address withCity(String city)
    {
        this.city = city;
        return this;
    }

    @JsonProperty("state")
    public String getState()
    {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state)
    {
        this.state = state;
    }

    public Address withState(String state)
    {
        this.state = state;
        return this;
    }

    @JsonProperty("postal_address")
    public String getPostalAddress()
    {
        return postalAddress;
    }

    @JsonProperty("postal_address")
    public void setPostalAddress(String postalAddress)
    {
        this.postalAddress = postalAddress;
    }

    public Address withPostalAddress(String postalAddress)
    {
        this.postalAddress = postalAddress;
        return this;
    }

    @JsonProperty("postal_code")
    public String getPostalCode()
    {
        return postalCode;
    }

    @JsonProperty("postal_code")
    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    public Address withPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
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

    public Address withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}