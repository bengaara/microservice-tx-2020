package net.tospay.transaction.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Store extends BaseModel implements Serializable
{

    @JsonProperty("account")
    private Account account;

    @JsonProperty("order")
    private Amount order;

    @JsonProperty("charge")
    private Amount charge;

    @JsonProperty(value = "total")
    private Amount total;//add when source.. sub when destination

}