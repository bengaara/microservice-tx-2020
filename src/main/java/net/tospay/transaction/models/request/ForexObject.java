package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.BaseModel;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ForexObject<T> extends BaseModel {


    @JsonProperty("origin")
    private Amount origin;
    @JsonProperty("destination")
    private Amount destination;
    @JsonProperty("fx_id")
    private String fxId;

    public ForexObject() {

    }

    public ForexObject(Amount origin, Amount destination) {
        this.origin = origin;
        this.destination = destination;
    }


}
