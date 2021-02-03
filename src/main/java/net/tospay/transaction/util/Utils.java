package net.tospay.transaction.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
public class Utils {


    public static DateTimeFormatter FORMATTER_DAY_MINI = DateTimeFormatter.ofPattern("ddMMMYY");
    public static DateTimeFormatter FORMATTER_DAY_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
    public static DateTimeFormatter FORMATTER_DAY = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static DateTimeFormatter FORMATTER_TIME = DateTimeFormatter.ofPattern("h:mm a");
    static Logger logger = LoggerFactory.getLogger(Utils.class);
    static ObjectMapper mapper = new ObjectMapper();

    /**
     * Gets a string from current date and time
     *
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
        } catch (Exception e) {
            logger.info("", e.getMessage());
            return "";
        }
    }

    public static Object deepCopy(Object object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            outputStrm.writeObject(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            return objInputStream.readObject();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public static String getCallerCallerClassName() {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String callerClassName = null;
        for (int i = 1; i < stElements.length; i++) {
            StackTraceElement ste = stElements[i];
            if (!ste.getClassName().equals(Utils.class.getName()) && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                if (callerClassName == null) {
                    callerClassName = ste.getClassName();
                } else if (!callerClassName.equals(ste.getClassName())) {
                    return ste.getClassName() + " - " + ste.getLineNumber();
                }
            }
        }
        return null;
    }

    public static String generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        String s = Base64.getUrlEncoder().withoutPadding().encodeToString(token); //base64 encoding
        logger.debug("generateRandomBase64Token {} {}", s,s.substring(0,byteLength));
        return s.substring(0,byteLength);
    }
    public static String generateSimpleRandomBase64Token(int byteLength) {
        String s = generateRandomBase64Token(byteLength);
        int rand1 = (int) (Math.random() * (byteLength)) ;
        int rand2 = (int) (Math.random() * (byteLength)) ;
        String ss= s.replace(s.charAt(rand1), s.charAt(rand2)).toUpperCase();//duplicate a few
        logger.debug("generateSimpleRandomBase64Token {} {}", s,ss);
        return ss;
    }
    public static String generateAlphanumeric(int length) {
        String s =   UUID.randomUUID().toString().replaceAll("-", "").substring(0,length);
        logger.debug("generateAlphanumeric {} {}", s);
        return s;
    }
    public static String generateSimpleAlphanumeric(int byteLength) {
        String s = generateAlphanumeric(byteLength);
        int rand1 = (int) (Math.random() * (byteLength)) ;
        int rand2 = (int) (Math.random() * (byteLength)) ;
        String ss= s.replace(s.charAt(rand1), s.charAt(rand2)).toUpperCase();//duplicate a few
        logger.debug("generateSimpleAlphanumeric {} {}", s,ss);
        return ss;
    }

}
