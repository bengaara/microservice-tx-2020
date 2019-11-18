package net.tospay.transaction.models.request;

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
public class UserInfo implements Serializable
{
    private final static long serialVersionUID = -1250620497979241622L;

    @JsonProperty("user_id")
    private UUID userId;

    @JsonProperty("type_id")
    private String typeId;

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

    @JsonProperty("user_id")
    public UUID getUserId()
    {
        return userId;
    }

    @JsonProperty("user_id")
    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    public UserInfo withUserId(UUID userId)
    {
        this.userId = userId;
        return this;
    }

    @JsonProperty("type_id")
    public String getTypeId()
    {
        return typeId;
    }

    @JsonProperty("type_id")
    public void setTypeId(String typeId)
    {
        this.typeId = typeId;
    }

    public UserInfo withTypeId(String typeId)
    {
        this.typeId = typeId;
        return this;
    }

    @JsonProperty("email")
    public String getEmail()
    {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email)
    {
        this.email = email;
    }

    public UserInfo withEmail(String email)
    {
        this.email = email;
        return this;
    }

    @JsonProperty("email_verified")
    public Boolean getEmailVerified()
    {
        return emailVerified;
    }

    @JsonProperty("email_verified")
    public void setEmailVerified(Boolean emailVerified)
    {
        this.emailVerified = emailVerified;
    }

    public UserInfo withEmailVerified(Boolean emailVerified)
    {
        this.emailVerified = emailVerified;
        return this;
    }

    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public UserInfo withName(String name)
    {
        this.name = name;
        return this;
    }

    @JsonProperty("phone")
    public String getPhone()
    {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public UserInfo withPhone(String phone)
    {
        this.phone = phone;
        return this;
    }

    @JsonProperty("phone_verified")
    public Boolean getPhoneVerified()
    {
        return phoneVerified;
    }

    @JsonProperty("phone_verified")
    public void setPhoneVerified(Boolean phoneVerified)
    {
        this.phoneVerified = phoneVerified;
    }

    public UserInfo withPhoneVerified(Boolean phoneVerified)
    {
        this.phoneVerified = phoneVerified;
        return this;
    }

    @JsonProperty("profile_pic")
    public String getProfilePic()
    {
        return profilePic;
    }

    @JsonProperty("profile_pic")
    public void setProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
    }

    public UserInfo withProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
        return this;
    }

    @JsonProperty("country")
    public Country getCountry()
    {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(Country country)
    {
        this.country = country;
    }

    public UserInfo withCountry(Country country)
    {
        this.country = country;
        return this;
    }

    @JsonProperty("address")
    public Address getAddress()
    {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address)
    {
        this.address = address;
    }

    public UserInfo withAddress(Address address)
    {
        this.address = address;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }

    public UserInfo withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}
