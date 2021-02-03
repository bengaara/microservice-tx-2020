package net.tospay.transaction.models;

import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DeviceInfo extends BaseModel
{
    @NotNull
    @JsonProperty("ip")
    private List<String> ip;

    @NotNull
    @JsonProperty("userAgent")
    private String userAgent;
}