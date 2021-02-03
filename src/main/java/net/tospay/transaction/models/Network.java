package net.tospay.transaction.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Network  extends BaseModel implements Serializable
{
    private final static long serialVersionUID = 7387601059382747089L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("mnc")
    private String mnc;

    @JsonProperty("mcc")
    private String mcc;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("operator")
    private String operator;

}