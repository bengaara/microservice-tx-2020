package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CommissionRequest extends BaseModel {

  @JsonProperty("wallet_id")
  private UUID walletId;
  @JsonProperty("user_id")
  private UUID userId;
  @JsonProperty("account_type")
  private UserType userType;
  @JsonProperty("amount")
  private Amount amount;

  @JsonProperty("transaction_date")
  private String transactionDate;
 // @JsonProperty("transaction_id")
 // private String transactionId;
  @JsonProperty("transaction_sub_type")
  private String transactionSubType;
  @JsonProperty("transaction_type")
  private TransactionType transactionType;


}