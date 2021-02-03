package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import net.tospay.transaction.models.BaseModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionFindRequest extends BaseModel {

  @JsonProperty("id")
  String id;

  @JsonProperty("from")
  LocalDate from;
  @JsonProperty("to")
  LocalDate to;
  @JsonProperty("offset")
  private Integer offset = 0;
  @JsonProperty("limit")
  private Integer limit = 10;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }
}