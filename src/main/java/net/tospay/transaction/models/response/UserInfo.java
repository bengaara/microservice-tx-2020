package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Address;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "user_id",
        "type_id",
        "email",
        "email_verified",
        "name",
        "phone",
        "phone_verified",
        "profile_pic",
        "country",
        "address"
})
@Data
public class UserInfo implements Serializable {
    private final static long serialVersionUID = -1250620497979241622L;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("type_id")
    private UserType typeId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("name")
    private String name;

    @JsonProperty("phone")
    private String phone;

    @JsonProperty("phone_verified")
    private Boolean phoneVerified;

    @JsonProperty("profile_pic")
    private String profilePic;

    @JsonProperty("country")
    private Country country;

    @JsonProperty("address")
    private Address address;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public UserInfo withUserId(UUID userId) {
        this.userId = userId;
        return this;
    }


    public UserInfo withTypeId(UserType typeId) {
        this.typeId = typeId;
        return this;
    }

    public UserInfo withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserInfo withEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
        return this;
    }


    public UserInfo withName(String name) {
        this.name = name;
        return this;
    }

    public UserInfo withPhone(String phone) {
        this.phone = phone;
        return this;
    }


    public UserInfo withPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
        return this;
    }

    public UserInfo withProfilePic(String profilePic) {
        this.profilePic = profilePic;
        return this;
    }

    public UserInfo withCountry(Country country) {
        this.country = country;
        return this;
    }

    public UserInfo withAddress(Address address) {
        this.address = address;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public UserInfo withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }
}
