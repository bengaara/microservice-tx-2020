package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class StoreStatusResponse extends BaseModel {
    private final static long serialVersionUID = -9078608771772465581L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private TransactionStatus status;
    @JsonProperty("code")
    private String code;
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("description")
    private String description;
    @JsonProperty("store_ref")
    private String storeRef;
    @JsonProperty("external_reference")
    private UUID externalReference;

    public AsyncCallbackResponse toAsyncCallbackResponse() {
        AsyncCallbackResponse response = new AsyncCallbackResponse();

        response.setExternalReference(this.getId() == null ? null : UUID.fromString(this.getId()));
        response.setStoreRef(this.getStoreRef());
        response.setReason(this.getReason());
        response.setDescription(this.getDescription());

        ResponseCode r = ResponseCode.valueOfType(this.getCode());
        if (r == null) {
            for (ResponseCode c : ResponseCode.values()) {
                if (c.name().equalsIgnoreCase(this.getCode())) {
                    r = c;
                    break;
                }
            }

        }
        response.setCode(r);
        return response;
    }
}