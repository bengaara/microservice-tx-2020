package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class SummaryStatement  {

    @JsonProperty("sent")
    public Number sent;
    @JsonProperty("received")
    public Number received;
    @JsonProperty("totalIncoming")
    public Number totalIncoming;
    @JsonProperty("totalOutgoing")
    public Number totalOutgoing;


}
