package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 8/1/2019, Thu
 **/
public enum TransactionType
{
    TOPUP("TOPUP"),
    TRANSFER("TRANSFER"),
    WITHDRAW("WITHDRAW"),
    PAYMENT("PAYMENT"),
    REVERSE("REVERSE"),
    SETTLEMENT("SETTLEMENT"),
    INVOICE("INVOICE");

    private static final Map<String, TransactionType> LABEL = new HashMap<>();

    static {
        for (TransactionType e : values()) {
            LABEL.put(e.type, e);
        }
    }

    private String type;

    // ... fields, constructor, methods

    TransactionType(String type)
    {
        this.type = type;
    }

    public static TransactionType valueOfType(String label)
    {
        return LABEL.get(label);
    }
}
