package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum ResponseCode
{
    SUCCESS("000"),
    FAILURE("001"),
    PROCESSING("002"),
    GENERAL_ERROR("999");

    private static final Map<String, ResponseCode> codeMap = new HashMap<>();

    static {
        for (ResponseCode e : values()) {
            codeMap.put(e.type, e);
        }
    }

     public final String type;

    private ResponseCode(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static ResponseCode valueOfType(String label)
    {
        label = label.toUpperCase();
        return codeMap.get(label);
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
