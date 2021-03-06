package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum TransactionStatus {
    CREATED("CREATED"),
    PROCESSING("PROCESSING"),
    FAILED("FAILED"),
    SUCCESS("SUCCESS");

    private static final Map<String, TransactionStatus> LABEL = new HashMap<>();

    static {
        for (TransactionStatus e : TransactionStatus.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    TransactionStatus(String type) {
        this.type = type;
    }

    @JsonCreator
    public static TransactionStatus valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }

    public int getCode() {
        int x = 0;
        for (Map.Entry<String, TransactionStatus> entry : LABEL.entrySet()) {
            String s = entry.getKey();
            TransactionStatus transactionStatus = entry.getValue();
            if (s == type) {
                return x;
            }
            x++;
        }
        return -1;
    }
}
