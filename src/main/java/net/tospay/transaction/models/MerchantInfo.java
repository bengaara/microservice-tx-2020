package net.tospay.transaction.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
import net.tospay.transaction.enums.UserType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "user_id",
        "type_id",
        "email",
        "name",
        "country_code",
        "phone",
        "profile_pic",
        "country",
        "address"
})
@Data
public class MerchantInfo extends BaseModel
{
    private final static long serialVersionUID = 4711076389786494367L;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("type_id")
    private UserType typeId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("profile_pic")
    private String profilePic;

    @JsonProperty("country")
    private Country country;

    @JsonProperty("address")
    private Address address;

}