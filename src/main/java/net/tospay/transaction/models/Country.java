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
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Country  extends BaseModel implements Serializable
{
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

}