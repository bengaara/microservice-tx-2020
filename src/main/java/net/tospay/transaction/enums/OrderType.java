package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum OrderType {

   SYSTEM_LOAD("SYSTEM_LOAD"),//SYS_OP_ACC
    SYSTEM_AGENT("SYSTEM_AGENT"),
    SYSTEM_MERCHANT("SYSTEM_MERCHANT"),
    AGENT_CUSTOMER("AGENT_CUSTOMER"),
    AGENT_AGENT("AGENT_AGENT"),
    AGENT_SYSTEM("AGENT_SYSTEM"),
    AGENT_MERCHANT("AGENT_MERCHANT"),
    MERCHANT_SYSTEM("MERCHANT_SYSTEM"),
    MERCHANT_MERCHANT("MERCHANT_MERCHANT"),
    MERCHANT_CUSTOMER("MERCHANT_CUSTOMER"),
    CUSTOMER_AGENT("CUSTOMER_AGENT"),
    CUSTOMER_CUSTOMER("CUSTOMER_CUSTOMER"),
    CUSTOMER_MERCHANT("CUSTOMER_MERCHANT"),

    ;

    private static final Map<String, OrderType> LABEL = new HashMap<>();

    static {
        for (OrderType e : OrderType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    OrderType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static OrderType valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }


}
