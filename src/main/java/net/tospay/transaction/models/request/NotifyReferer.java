package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class NotifyReferer {
    @JsonProperty("user_id")
    UUID userId;
    @JsonProperty("user_type")
    String userType;

    public NotifyReferer() {

    }

    public NotifyReferer(UUID userId, String userType) {
        this.userId = userId;
        this.userType = userType;
    }

}