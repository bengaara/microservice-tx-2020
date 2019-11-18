package net.tospay.transaction.util;

import java.time.LocalDateTime;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
public class Utils {

    /**
     * Gets a string from current date and time
     * @return String
     */
    public static String getTimestamp() {
        return LocalDateTime.now().toString().replaceAll("\\D", "");
    }
}
