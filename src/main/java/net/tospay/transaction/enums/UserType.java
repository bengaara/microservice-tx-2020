package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.Map;

public enum UserType {
    PERSONAL("PERSONAL"),
    AGENT("AGENT"),
    MERCHANT("MERCHANT"),
    ADMIN("ADMIN"),
    PARTNER("PARTNER"),
    SYSTEM("SYSTEM");

    private static final Map<String, UserType> LABEL = new HashMap<>();

    static {
        for (UserType e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    UserType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static UserType valueOfType(String label) {
        label = label.toUpperCase();
        return LABEL.get(label);

    }
}
