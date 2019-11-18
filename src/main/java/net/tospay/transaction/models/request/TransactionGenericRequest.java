package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.TransactionType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "type",
        "merchantInfo",
        "amount",
        "currency"
})
public class TransactionGenericRequest implements Serializable
{
    private final static long serialVersionUID = -5739806967313718851L;

    @JsonProperty("type")
    private TransactionType type;

    @JsonProperty("merchantInfo")
    private UUID merchantInfo;

    // @JsonProperty("amount")
    //  private Amount amount;

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("currency")
    private String currency;

    public static long getSerialVersionUID()
    {
        return serialVersionUID;
    }

    public TransactionType getType()
    {
        return type;
    }

    public void setType(TransactionType type)
    {
        this.type = type;
    }

    public UUID getMerchantInfo()
    {
        return merchantInfo;
    }

    public void setMerchantInfo(UUID merchantInfo)
    {
        this.merchantInfo = merchantInfo;
    }

    public Double getAmount()
    {
        return amount;
    }

    public void setAmount(Double amount)
    {
        this.amount = amount;
    }

    public String getCurrency()
    {
        return currency;
    }

    public void setCurrency(String currency)
    {
        this.currency = currency;
    }
}