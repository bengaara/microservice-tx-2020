package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.models.BaseModel;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)//Include.NON_EMPTY
//@JsonPropertyOrder({
//        "transactionId",
//})
public class StatementResponse extends BaseModel {
    // static Logger logger = LoggerFactory.getLogger(StatementResponse.class);
    // @formatter:off
    @JsonProperty("summary")
    SummaryStatement summaryStatement;
    @JsonProperty("items")
    List<StatementItem> items = new ArrayList<>();
    @JsonProperty("customer")
    private UserInfoStatement customer;

    public UserInfoStatement getCustomer() {
        return customer;
    }

    public void setCustomer(UserInfoStatement customer) {
        this.customer = customer;
    }

    public SummaryStatement getSummaryStatement() {
        return summaryStatement;
    }

    public void setSummaryStatement(SummaryStatement summaryStatement) {
        this.summaryStatement = summaryStatement;
    }

    public List<StatementItem> getItems() {
        return items;
    }

    public void setItems(List<StatementItem> items) {
        this.items = items;
    }
// @formatter:on


}
