package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AsyncCallbackResponse extends BaseModel   implements Serializable {
    private final static long serialVersionUID = -9078608771772465581L;


    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("channel")
    private AccountType channel;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("reason")
    private String reason;
    @JsonProperty("code")
    private ResponseCode code;

    @JsonProperty("description")
    private String description;
    @JsonProperty("store_ref")
    private String storeRef;
    @JsonProperty("external_reference")
    private UUID externalReference;

    @JsonProperty("instructions")
    private String instructions;


}