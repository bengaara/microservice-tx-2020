package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum StoreActionType
{

    DEBIT("DEBIT"),
    CREDIT("CREDIT");

    private static final Map<String, StoreActionType> LABEL = new HashMap<>();

    static {
        for (StoreActionType e : StoreActionType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    StoreActionType(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static StoreActionType valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
