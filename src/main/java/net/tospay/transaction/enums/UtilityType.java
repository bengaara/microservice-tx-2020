package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum UtilityType {

    QR("QR"),
    INVOICE("INVOICE"),
    PAYBILL("PAYBILL"),
    SPLIT("SPLIT"),


    CHECKOUT("CHECKOUT"),
    AIRTIME("BUY_AIRTIME"),
    BUNDLE("BUY_BUNDLE"),

    AGENT("AGENT"),
    KPLC("KPLC"),
    GOTV("GOTV"),

    MMI("MMI"),
    WSO2("WSO2"),

    ;

    private static final Map<String, UtilityType> LABEL = new HashMap<>();

    static {
        for (UtilityType e : UtilityType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    UtilityType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static UtilityType valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }


}
