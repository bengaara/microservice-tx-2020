package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

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

    @JsonCreator
    public static TransactionType valueOfType(String label)
    {

        label = label.toUpperCase();
        return LABEL.get(label);
    }
}
