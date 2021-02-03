package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ChargeUser extends BaseModel
{
    @JsonProperty("amount")
    private Amount amount;

    @JsonProperty("account")
    private Account account;


}