package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum FraudStatus {
    PROCESSING("PROCESSING"),
    PROCEED("PROCEED"),
    DO_NOT_PROCEED("DO_NOT_PROCEED"),
    SHOW_3DES("SHOW_3DES");

    private static final Map<String, FraudStatus> LABEL = new HashMap<>();

    static {
        for (FraudStatus e : FraudStatus.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    FraudStatus(String type) {
        this.type = type;
    }

    @JsonCreator
    public static FraudStatus valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }

    public int getCode() {
        int x = 0;
        for (Map.Entry<String, FraudStatus> entry : LABEL.entrySet()) {
            String s = entry.getKey();
            FraudStatus transactionStatus = entry.getValue();
            if (s == type) {
                return x;
            }
            x++;
        }
        return -1;
    }
}
