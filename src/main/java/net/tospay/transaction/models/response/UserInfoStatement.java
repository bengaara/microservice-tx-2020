package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.util.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class UserInfoStatement extends UserInfo {

    @JsonProperty("date")
    LocalDateTime date;
    @JsonProperty("dateFormatted")
    String dateFormatted;
    @JsonProperty("from")
    LocalDate from;
    @JsonProperty("fromFormatted")
    String fromFormatted;
    @JsonProperty("to")
    LocalDate to;
    @JsonProperty("toFormatted")
    String toFormatted;


    public String getDateFormatted() {
        return dateFormatted;
    }

    public String getFromFormatted() {
        return fromFormatted;
    }

    public String getToFormatted() {
        return toFormatted;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
        if (date != null)
            this.dateFormatted = Utils.FORMATTER_DAY_TIME.format(date.atZone(ZoneId.systemDefault())
                    .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime());
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
        if (from != null)
            this.fromFormatted = Utils.FORMATTER_DAY.format(from);
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
        if (to != null)
            this.toFormatted = Utils.FORMATTER_DAY.format(to);
    }
}
