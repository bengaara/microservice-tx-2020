package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderType
{

    QR("QR"),
    INVOICE("INVOICE"),
    PAYBILL("PAYBILL"),
    SPLIT("SPLIT");

    private static final Map<String, OrderType> LABEL = new HashMap<>();

    static {
        for (OrderType e : OrderType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    OrderType(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static OrderType valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
