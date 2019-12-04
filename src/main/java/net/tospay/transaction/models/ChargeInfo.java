package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.Fx;
import net.tospay.transaction.models.response.MerchantInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChargeInfo
{



    @JsonProperty("source")
    private Amount source;
    @JsonProperty("destination")
    private Amount destination;

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

    public Account getPartnerInfo()
    {
        return partnerInfo;
    }

    public void setPartnerInfo(Account partnerInfo)
    {
        this.partnerInfo = partnerInfo;
    }

    public Account getRailInfo()
    {
        return railInfo;
    }

    public void setRailInfo(Account railInfo)
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

    @JsonProperty("partnerInfo")
    private Account partnerInfo;

    @JsonProperty("railInfo")
    private Account railInfo;

    @JsonProperty("fx")
    private Fx fx;

}