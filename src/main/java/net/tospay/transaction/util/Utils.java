package net.tospay.transaction.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
public class Utils {

    public static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");

    /**
     * Gets a string from current date and time
     * @return String
     */
    public static String getTimestamp() {
        return LocalDateTime.now().toString().replaceAll("\\D", "");
    }
}
