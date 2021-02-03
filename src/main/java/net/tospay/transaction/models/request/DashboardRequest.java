package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDate;
import lombok.Data;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.UserInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
})
@Data
public class DashboardRequest extends BaseModel {

    @JsonProperty("to")
    LocalDate to;// 2020-01-12 //  2020-01-12T00:00
    @JsonProperty("from")
    LocalDate from;
    @JsonProperty("userInfo")
    UserInfo userInfo;

    @JsonProperty("limit")
    Integer limit;
    @JsonProperty("offset")
    Integer offset;

    @JsonProperty("groupType")
    GroupType groupType;

    @JsonProperty("currency")
    String currency;

    @JsonProperty("user_id")
    String userId;


    public enum GroupType {
        DAY("DAY"),
        WEEK("WEEK"),
        MONTH("MONTH"),
        YEAR("YEAR");

        private static final Map<String, GroupType> LABEL = new HashMap<>();

        static {
            for (GroupType e : GroupType.values()) {
                LABEL.put(e.type, e);
            }
        }

        private final String type;

        // ... fields, constructor, methods

        GroupType(String type) {
            this.type = type;
        }

        @JsonCreator
        public static GroupType valueOfType(String label) {
            label = label.toUpperCase();
            return LABEL.get(label);
        }
    }
}