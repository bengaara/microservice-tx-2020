package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Transfer
{
    public enum TransferAction
    {
        SOURCE("SOURCE"),
        DESTINATION("DESTINATION");

        private static final Map<String, TransferAction> LABEL = new HashMap<>();

        static {
            for (TransferAction e : TransferAction.values()) {
                LABEL.put(e.type, e);
            }
        }

        private String type;

        // ... fields, constructor, methods

        TransferAction(String type)
        {
            this.type = type;
        }

        @JsonCreator
        public static TransferAction valueOfType(String label)
        {

            label = label.toUpperCase();
            return LABEL.get(label);
        }
    }

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
            for (TransactionType e : TransactionType.values()) {
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

    /**
     * @author : Clifford Owino
     * @Email : owinoclifford@gmail.com
     * @since : 8/1/2019, Thu
     **/
    public enum SourceType
    {
        MOBILE("MOBILE"),
        BANK("BANK"),
        WALLET("WALLET"),
        CARD("CARD");

        private static final Map<String, SourceType> LABEL = new HashMap<>();

        static {
            for (SourceType e : SourceType.values()) {
                LABEL.put(e.type, e);
            }
        }

        private String type;

        // ... fields, constructor, methods

        SourceType(String type)
        {
            this.type = type;
        }

        @JsonCreator
        public static SourceType valueOfType(String label)
        {
            label = label.toUpperCase();
            return LABEL.get(label);
        }
    }

    public enum TransactionStatus
    {
        CREATED("CREATED"),
        PROCESSING("PROCESSING"),
        FAILED("FAILED"),
        SUCCESS("SUCCESS"),
        PARTIAL_COMPLETE("PARTIAL_COMPLETE");

        private static final Map<String, TransactionStatus> LABEL = new HashMap<>();

        static {
            for (TransactionStatus e : TransactionStatus.values()) {
                LABEL.put(e.type, e);
            }
        }

        private String type;

        // ... fields, constructor, methods

        TransactionStatus(String type)
        {
            this.type = type;
        }

        @JsonCreator
        public static TransactionStatus valueOfType(String label)
        {

            label = label.toUpperCase();
            return LABEL.get(label);
        }
    }
}
