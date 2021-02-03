package net.tospay.transaction.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.UserType;
import org.hibernate.annotations.Type;


//@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Account extends BaseModel  implements Serializable {
    @JsonProperty("account_type")
    String accountType;
    @Enumerated(EnumType.STRING)
    @JsonProperty("type")
    private AccountType type;
    @JsonProperty("sub_type")
    private AccountType.AccountSubType subType;

    //holds string or uuid
    @JsonProperty("id")
    private String id;
    @JsonProperty("user_id")
  //  private UUID userId;
    @Type(type = "org.hibernate.type.PostgresUUIDType")//coz of hibernate fetch directly
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @JsonProperty("user_type")
    private UserType userType;
    @JsonProperty("country")
    private Country country;
    @JsonProperty("network")
    private Network network;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("callback_url")
    private String callbackUrl;


}