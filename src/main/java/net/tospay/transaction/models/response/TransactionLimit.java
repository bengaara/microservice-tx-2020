package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransactionLimit extends BaseModel {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("max_balance")
    private Amount maxBalance;

    @JsonProperty("txn_lower_limit")
    private Amount txnLowerLimit;

    @JsonProperty("txn_upper_limit")
    private Amount txnUpperLimit;

    @JsonProperty("daily_limit")
    private Amount dailyLimit;

    @JsonProperty("max_daily_transactions")
    private Number maxDailyTransactions;

    @JsonProperty("allowed_transactions")
    private List<String> allowedTransactions;

    @JsonProperty("balance")
    private Amount balance;

    @JsonProperty("airtime_limit")
    private Amount airtimeLimit;
    @JsonProperty("bundle_limit")
    private Amount bundleLimit;

}
