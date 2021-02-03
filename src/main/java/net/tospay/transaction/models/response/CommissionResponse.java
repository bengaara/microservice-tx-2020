package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CommissionResponse extends BaseModel {

  @JsonProperty("account_type")
  private UserType userType;
  @JsonProperty("user_id")
  private UUID userId;
  @JsonProperty("wallet_id")
  private UUID walletId;
  @JsonProperty("amount")
  private Amount amount;

}