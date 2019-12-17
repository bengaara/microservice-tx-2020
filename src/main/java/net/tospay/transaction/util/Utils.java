package net.tospay.transaction.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
public class Utils {

    public static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");
    static Logger logger = LoggerFactory.getLogger(Utils.class);
    static ObjectMapper mapper = new ObjectMapper();
    /**
     * Gets a string from current date and time
     * @return String
     */
    public static String getTimestamp() {
        return LocalDateTime.now().toString().replaceAll("\\D", "");
    }

    public static String inspect(Object object) {
        try {
//            Field[] fields = object.getClass().getDeclaredFields();
//            logger.debug("%d fields:%n", fields.length);
//            for (Field field : fields) {
//                logger.debug("%s %s %s%n", Modifier.toString(field.getModifiers()),
//                        field.getType().getSimpleName(),
//                        field.getName()
//                );
//            }
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }catch(Exception e){
            logger.error("",e);
            return "";
        }
    }
}
