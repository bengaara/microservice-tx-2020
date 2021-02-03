package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.Data;
import net.tospay.transaction.enums.UserType;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class UserInfo extends BaseModel {
    @JsonProperty("user_id")
   // private UUID userId;
    private UUID userId;

    @JsonProperty("type_id")
    private UserType typeId;
    @JsonProperty("account_no")
    private String accountNo;
    @JsonProperty("email")
    private String email;
    @JsonProperty("email_verified")
    private Boolean emailVerified;
    @JsonProperty("name")
    private String name;
    @JsonProperty("phone")
    private String phone;

    @JsonProperty("msisdn")
    private String msisdn;
    @JsonProperty("agent_id")
    private UUID agentId;

    @JsonProperty("partner_id")
    private UUID partnerId;

    @JsonProperty("phone_verified")
    private Boolean phoneVerified;
    @JsonProperty("profile_pic")
    private String profilePic;
    @JsonProperty("country")
    private Country country;
    @JsonProperty("address")
    private Address address;

    public Account asAccount(){
        Account account = new Account();
        account.setEmail(this.getEmail());
        account.setUserType(this.getTypeId());
        account.setUserId(this.getUserId());
        account.setCountry(this.getCountry());
        account.setName(this.getName());
        account.setPhone(this.getPhone());
//        account.setAccountType();
//        account.setId();
//        account.setNetwork();
//        account.setPartnerId();
//        account.setSubType();
//        account.setType();
//        account.setCallbackUrl();
        return account;
    }
}
