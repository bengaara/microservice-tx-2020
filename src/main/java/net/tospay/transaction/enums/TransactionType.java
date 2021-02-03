package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum TransactionType {
    TOPUP("TOPUP"),
    TRANSFER("TRANSFER"),
    WITHDRAWAL("WITHDRAWAL"),
    REVERSAL("REVERSAL"),
    SETTLEMENT("SETTLEMENT"),
    PAYMENT("PAYMENT"),
    UTILITY("UTILITY"),
    ;


    private static final Map<String, TransactionType> LABEL = new HashMap<>();

    static {
        for (TransactionType e : TransactionType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    TransactionType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static TransactionType valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
