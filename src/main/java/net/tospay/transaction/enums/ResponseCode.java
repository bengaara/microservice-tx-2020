package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 7/16/2019, Tue
 **/
public enum ResponseCode
{
    SUCCESS("0"),
    GENERAL_ERROR("999");

    private static final Map<String, ResponseCode> codeMap = new HashMap<>();

    static {
        for (ResponseCode e : values()) {
            codeMap.put(e.code, e);
        }
    }

    public final String code;

    private ResponseCode(String code)
    {
        this.code = code;
    }

    public static ResponseCode valueOfCode(String code)
    {
        return codeMap.get(code);
    }
//    public static CoopResponseCode valueOfCode(String code)
//    {
//        for (CoopResponseCode e : values()) {
//            if (e.code.equals(code)) {
//                return e;
//            }
//        }
//        return null;
//    }
}
