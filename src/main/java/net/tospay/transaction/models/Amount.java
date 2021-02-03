package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Amount extends BaseModel  implements Serializable {

    // @JsonDeserialize(using = BigDecimalMoneyDeserializer.class)//(using = BigDecimalMoneyDeserializer.class)
    // @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = String.class)
    @JsonDeserialize(as = BigDecimal.class)
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    public Amount() {

    }
    public Amount(Amount amount) {
        if (amount != null) {
            this.amount = amount.getAmount();
            this.currency = amount.getCurrency();
        }

    }

    public Amount(BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }


    public Amount withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }
}