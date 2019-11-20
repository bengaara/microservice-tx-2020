package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionStatus
{
    CREATED("CREATED"),
    PROCESSING("PROCESSING"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS");

    private static final Map<String, TransactionStatus> LABEL = new HashMap<>();

    static {
        for (TransactionStatus e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    TransactionStatus(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static TransactionStatus valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
