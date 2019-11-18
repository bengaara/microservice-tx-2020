package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 8/1/2019, Thu
 **/
public enum SourceType
{
    MOBILE("mobile"),
    BANK("bank"),
    WALLET("wallet"),
    CARD("card");

    private static final Map<String, SourceType> LABEL = new HashMap<>();

    static {
        for (SourceType e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    SourceType(String type)
    {
        this.type = type;
    }

    public static SourceType valueOfType(String label)
    {
        return LABEL.get(label);
    }
}
