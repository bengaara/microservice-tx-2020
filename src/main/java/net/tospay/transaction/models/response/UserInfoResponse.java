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
        "status",
        "description",
        "data"
})
public class UserInfoResponse implements Serializable
{
    private final static long serialVersionUID = -2158148432338648710L;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("data")
    private Data data;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("status")
    public String getStatus()
    {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status)
    {
        this.status = status;
    }

    public UserInfoResponse withStatus(String status)
    {
        this.status = status;
        return this;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description)
    {
        this.description = description;
    }

    public UserInfoResponse withDescription(String description)
    {
        this.description = description;
        return this;
    }

    @JsonProperty("data")
    public Data getData()
    {
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data)
    {
        this.data = data;
    }

    public UserInfoResponse withData(Data data)
    {
        this.data = data;
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

    public UserInfoResponse withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}