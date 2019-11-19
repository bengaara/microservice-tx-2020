package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType
{
    PERSONAL("PERSONAL"),
    AGENT("AGENT"),
    MERCHANT("MERCHANT"),
    PARTNER("PARTNER");

    private static final Map<String, AccountType> LABEL = new HashMap<>();

    static {
        for (AccountType e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    AccountType(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static AccountType valueOfType(String label)
    {
        return LABEL.get(label);
    }
}
