package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

public enum TopupAction
{
    SOURCE("SOURCE"),
    DESTINATION("DESTINATION");

    private static final Map<String, TopupAction> LABEL = new HashMap<>();

    static {
        for (TopupAction e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    TopupAction(String type)
    {
        this.type = type;
    }

    public static TopupAction valueOfType(String label)
    {
        return LABEL.get(label);
    }
}
