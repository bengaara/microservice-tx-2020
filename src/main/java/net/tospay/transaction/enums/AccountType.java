package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType
{
    MOBILE("MOBILE"),
    BANK("BANK"),
    WALLET("WALLET"),
    CARD("CARD");

    private static final Map<String, AccountType> LABEL = new HashMap<>();

    static {
        for (AccountType e : AccountType.values()) {
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
        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
