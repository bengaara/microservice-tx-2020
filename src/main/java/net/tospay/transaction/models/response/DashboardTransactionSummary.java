package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.*;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardTransactionSummary extends BaseModel {
    //static Logger logger = LoggerFactory.getLogger(DashboardTransactionSummary.class);

    @JsonProperty("transaction")
    private DashboardTransactionSummaryChild transaction;
    @JsonProperty("revenue")
    private DashboardTransactionSummaryChild revenue;

    public DashboardTransactionSummaryChild getTransaction() {
        return transaction;
    }

    public void setTransaction(DashboardTransactionSummaryChild transaction) {
        this.transaction = transaction;
    }

    public DashboardTransactionSummaryChild getRevenue() {
        return revenue;
    }

    public void setRevenue(DashboardTransactionSummaryChild revenue) {
        this.revenue = revenue;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DashboardTransactionSummaryChild extends BaseModel {

        @JsonProperty("prev_count")
        private BigDecimal prevCount;
        @JsonProperty("current_count")
        private BigDecimal currentCount;
        @JsonProperty("percentage_change")
        private BigDecimal percentageChange;
        @JsonProperty("trend")
        private List<Object[]> trend;


        public BigDecimal getPrevCount() {
            return prevCount;
        }

        public void setPrevCount(BigDecimal prevCount) {
            this.prevCount = prevCount;
        }

        public BigDecimal getCurrentCount() {
            return currentCount;
        }

        public void setCurrentCount(BigDecimal currentCount) {
            this.currentCount = currentCount;
        }

        public BigDecimal getPercentageChange() {
            return percentageChange;
        }

        public void setPercentageChange(BigDecimal percentageChange) {
            this.percentageChange = percentageChange;
        }

        public List<Object[]> getTrend() {
            return trend;
        }

        public void setTrend(List<Object[]> trend) {
            this.trend = trend;
        }
    }

}
