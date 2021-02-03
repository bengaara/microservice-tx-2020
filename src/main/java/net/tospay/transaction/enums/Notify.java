package net.tospay.transaction.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.HashMap;
import java.util.Map;

public class Notify {
    public enum Category {
        CASHOUT("CASHOUT"),
        CASHIN("CASHIN"),
        COMMISSION("COMMISSION"),
        MINI_STATEMENT("MINI_STATEMENT"),
        FULL_STATEMENT("FULL_STATEMENT"),
        MT940("MT940"),
        UTILITY("UTILITY"),
        REVERSAL_CASHOUT("REVERSAL_CASHOUT"),
        REVERSAL_CASHIN("REVERSAL_CASHIN"),

        ;

        private static final Map<String, Category> LABEL = new HashMap<>();

        static {
            for (Category e : values()) {
                LABEL.put(e.type, e);
            }
        }

        private final String type;

        // ... fields, constructor, methods

        Category(String type) {
            this.type = type;
        }

        @JsonCreator
        public static Category valueOfType(String label) {
            label = label.toUpperCase();
            return LABEL.get(label);

        }

        public static Category getCategory(TransactionType transactionType, StoreActionType storeActionType, boolean isCommision) {
            switch (transactionType) {

                //    break;
                case TRANSFER:
                case WITHDRAWAL:
                case TOPUP:
                    if (isCommision) {
                        return COMMISSION;
                    }
                    return StoreActionType.DEBIT.equals(storeActionType) ? CASHOUT : CASHIN;
                case UTILITY:
                    return StoreActionType.DEBIT.equals(storeActionType) ? UTILITY : CASHIN;

                case PAYMENT:
                case SETTLEMENT:
                    return StoreActionType.DEBIT.equals(storeActionType) ? CASHOUT : CASHIN;

                case REVERSAL:

                    return StoreActionType.DEBIT.equals(storeActionType) ? REVERSAL_CASHOUT : REVERSAL_CASHIN;

                default:
                    return null;
            }

        }
    }
}
