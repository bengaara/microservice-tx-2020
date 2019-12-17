package net.tospay.transaction.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardOrderInfo extends BaseModel
{
//    @JsonProperty("type")
//    private OrderType type;

//    @JsonProperty("amount")
//    private Amount amount;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("charge")
    private Charge charge;

    @JsonProperty("description")
    private String description;

    @JsonProperty("reference")
    private String reference;

    public static CardOrderInfo from(Destination d)
    {
        CardOrderInfo cardOrderInfo = new CardOrderInfo();
        cardOrderInfo.setAmount(d.getPayload().getTotal().getAmount());
        cardOrderInfo.setCurrency(d.getPayload().getTotal().getCurrency());
        Charge c = new Charge();
        c.amount = BigDecimal.ZERO;
        c.currency = d.getPayload().getTotal().getCurrency();
        cardOrderInfo.setCharge(c);
        cardOrderInfo.setReference(d.getId().toString());
        // cardOrderInfo.setDescription();
        return cardOrderInfo;
    }

    public static CardOrderInfo from(Source d)
    {
        CardOrderInfo cardOrderInfo = new CardOrderInfo();
        cardOrderInfo.setAmount(d.getPayload().getTotal().getAmount());
        cardOrderInfo.setCurrency(d.getPayload().getTotal().getCurrency());
        Charge c = new Charge();
        c.amount = BigDecimal.ZERO;
        c.currency = d.getPayload().getTotal().getCurrency();
        cardOrderInfo.setCharge(c);
        cardOrderInfo.setReference(d.getId().toString());
        // cardOrderInfo.setDescription();
        return cardOrderInfo;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
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

    public Charge getCharge()
    {
        return charge;
    }

    public void setCharge(Charge charge)
    {
        this.charge = charge;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

    static class Charge
    {
        @JsonProperty("amount")
        public BigDecimal amount;

        @JsonProperty("currency")
        public String currency;
    }
}