package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ChargeInfo extends BaseModel
{
    @JsonProperty("source")
    private Amount source;

    @JsonProperty("destination")
    private Amount destination;

    @JsonProperty("partnerInfo")
    private ChargeUser partnerInfo;

    @JsonProperty("railInfo")
    private ChargeUser railInfo;

}