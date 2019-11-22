package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Notify
{
    public enum Category
    {
        TRANSFER("TRANSFER"),
        LINK_CARD("LINK_CARD");

        private static final Map<String, Category> LABEL = new HashMap<>();

        static {
            for (Category e : values()) {
                LABEL.put(e.type, e);
            }
        }

        private String type;

        // ... fields, constructor, methods

        Category(String type)
        {
            this.type = type;
        }

        @JsonCreator
        public static Category valueOfType(String label)
        {
            label = label.toUpperCase();
            return LABEL.get(label);

        }
    }
}
