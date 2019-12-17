package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TransactionType
{
    TOPUP("TOPUP"),
    TRANSFER("TRANSFER"),
    WITHDRAW("WITHDRAW"),
    REVERSAL("REVERSAL"),
    SETTLEMENT("SETTLEMENT"),
    PAYMENT("PAYMENT");
   ;

    private static final Map<String, TransactionType> LABEL = new HashMap<>();

    static {
        for (TransactionType e : TransactionType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    TransactionType(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static TransactionType valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
