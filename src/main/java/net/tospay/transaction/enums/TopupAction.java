package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

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

    @JsonCreator
    public static TopupAction valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
