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

    @JsonProperty("type_id")
    public UserType getTypeId()
    {
        return typeId;
    }

    @JsonProperty("type_id")
    public void setTypeId(UserType typeId)
    {
        this.typeId = typeId;
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

    public MerchantInfo withEmail(String email)
    {
        this.email = email;
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

    public MerchantInfo withName(String name)
    {
        this.name = name;
        return this;
    }

    @JsonProperty("country_code")
    public String getCountryCode()
    {
        return countryCode;
    }

    @JsonProperty("country_code")
    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    public MerchantInfo withCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
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

    public MerchantInfo withPhone(String phone)
    {
        this.phone = phone;
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

    public MerchantInfo withProfilePic(String profilePic)
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

    public MerchantInfo withCountry(Country country)
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

    public MerchantInfo withAddress(Address address)
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

    public MerchantInfo withAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
        return this;
    }
}