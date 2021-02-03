package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import net.tospay.transaction.models.UserInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportFetchRequest {

    @JsonProperty("userInfo")
    UserInfo userInfo;
    @JsonProperty("from")
    LocalDate from;
    @JsonProperty("to")
    LocalDate to;
    //   @JsonProperty("email")
    //   List<String> email;
    @JsonProperty("docType")
    String docType;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

}