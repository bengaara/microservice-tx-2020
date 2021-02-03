package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum MakerCheckerStatus {
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECT("REJECT"),
  //  VOID("VOID"),
    ;

    private static final Map<String, MakerCheckerStatus> LABEL = new HashMap<>();

    static {
        for (MakerCheckerStatus e : MakerCheckerStatus.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    MakerCheckerStatus(String type) {
        this.type = type;
    }

    @JsonCreator
    public static MakerCheckerStatus valueOfType(String label) {

        label = label.toUpperCase();
        return LABEL.get(label);
    }

    public int getCode() {
        int x = 0;
        for (Map.Entry<String, MakerCheckerStatus> entry : LABEL.entrySet()) {
            String s = entry.getKey();
            MakerCheckerStatus transactionStatus = entry.getValue();
            if (s == type) {
                return x;
            }
            x++;
        }
        return -1;
    }
}
