package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeInfo
{
    @JsonProperty("source")
    private Amount source;

    @JsonProperty("destination")
    private Amount destination;

    @JsonProperty("partnerInfo")
    private ChargeUser partnerInfo;

    @JsonProperty("railInfo")
    private ChargeUser railInfo;

    @JsonProperty("fx")
    private Fx fx;

    public Amount getSource()
    {
        return source;
    }

    public void setSource(Amount source)
    {
        this.source = source;
    }

    public Amount getDestination()
    {
        return destination;
    }

    public void setDestination(Amount destination)
    {
        this.destination = destination;
    }

    public ChargeUser getPartnerInfo()
    {
        return partnerInfo;
    }

    public void setPartnerInfo(ChargeUser partnerInfo)
    {
        this.partnerInfo = partnerInfo;
    }

    public ChargeUser getRailInfo()
    {
        return railInfo;
    }

    public void setRailInfo(ChargeUser railInfo)
    {
        this.railInfo = railInfo;
    }

    public Fx getFx()
    {
        return fx;
    }

    public void setFx(Fx fx)
    {
        this.fx = fx;
    }
}