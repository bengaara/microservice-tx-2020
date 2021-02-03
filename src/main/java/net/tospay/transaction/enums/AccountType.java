package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public enum AccountType {
    MOBILE("MOBILE"),
    BANK("BANK"),
    WALLET("WALLET"),
    CARD("CARD");

    private static final Map<String, AccountType> LABEL = new HashMap<>();

    static {
        for (AccountType e : AccountType.values()) {
            LABEL.put(e.type, e);
        }
    }

    private final String type;

    // ... fields, constructor, methods

    AccountType(String type) {
        this.type = type;
    }

    @JsonCreator
    public static AccountType valueOfType(String label) {
        label = label.toUpperCase();
        return LABEL.get(label);
    }

    public enum AccountSubType {

        FLOAT("FLOAT"),
        COMMISSION("COMMISSION"),
        TRANSACTIONAL("TRANSACTIONAL"),
        TKASH("TKASH"),
        TRUECALLER("TRUECALLER"),

        GLOBAL_TOTAL_VALUE("GLOBAL_TOTAL_VALUE"),

        MPESA("MPESA"),
        //   TKASH("TKASH"), TODO: same name needed here too

        KCB("KCB"),
        NBK("NBK"),
        PESALINK("PESALINK"),


        VISA("VISA"),
        MASTERCARD("MASTERCARD"),
        AMEX("AMEX"),
        DISCOVER("DISCOVER"),
        ;

        private static final Map<String, AccountSubType> LABEL = new HashMap<>();

        static {
            for (AccountSubType e : AccountSubType.values()) {
                LABEL.put(e.type, e);
            }
        }

        private final String type;

        // ... fields, constructor, methods

        AccountSubType(String type) {
            this.type = type;
        }

        @JsonCreator
        public static AccountSubType valueOfType(String label) {
            label = label.toUpperCase();
            return LABEL.get(label);
        }
    }

}
