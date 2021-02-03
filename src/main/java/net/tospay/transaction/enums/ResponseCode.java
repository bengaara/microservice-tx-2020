package net.tospay.transaction.enums;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;


public enum ResponseCode
{

    SUCCESS("000"),
    FAILURE("001"),
    PROCESSING("002"),
    GENERAL_ERROR("899"),
//--------------------------------

    TRANSACTION_NOT_FOUND("801"),

    SOURCE_MATCHES_DESTINATION("803"),
    SOURCE_AMOUNT_LESS_THAN_MINIMUM("804"),
    DESTINATION_AMOUNT_LESS_THAN_MINIMUM("805"),
    AMOUNT_MISMATCH("806"),
    CURRENCY_MISMATCH("807"),
    SOURCE_OR_DESTINATION_MISSING("808"),
    SOURCE_ID_REQUIRED("809"),
    DESTINATION_ID_REQUIRED("810"),
    PAYMENT_TOKEN_MISSING("811"),
    TRANSACTION_TYPE_MISSING("812"),
    ORDER_INFO_MISSING("813"),
    REFERENCE_ID_IN_USE("814"),


    TRANSACTION_LIMIT_CHECK_FAILED("821"),
    FOREX_UNREACHABLE("822"),
    TRANSACTION_SOURCE_LOWER_LIMIT_REACHED("823"),
    TRANSACTION_SOURCE_UPPER_LIMIT_REACHED("824"),
    TRANSACTION_DELIVERY_LOWER_LIMIT_REACHED("825"),
    TRANSACTION_DELIVERY_UPPER_LIMIT_REACHED("826"),
    WALLET_DELIVERY_LIMIT_REACHED("827"),
    TRANSACTION_SOURCE_RATE_LIMIT_REACHED("828"),
    TRANSACTION_DELIVERY_RATE_LIMIT_REACHED("829"),



    REVERSAL_ALREADY_QUEUED("831"),
    REVERSAL_TIME_EXCEEDED("832"),
    REVERSAL_NOT_AUTHORISED("833"),
    REVERSAL_AMOUNT_EXCEEDED("834"),
    REVERSAL_NOT_FOUND("835"),
    REVERSAL_FAILED("836"),
    REVERSAL_NOT_AUTHORISED_TRANSACTION_FAILED("837"),


    CHANGES_ALREADY_QUEUED("841"),
    APPROVAL_STATUS_NOT_ALLOWED("842"),
    APPROVAL_STATUS_NOT_UPDATABLE("843"),
    MAKER_CANT_BE_CHECKER("844"),

    TRANSACTION_UTILITY_LIMIT_REACHED("851"),
    WITHDRAWAL_DISTANCE_EXCEEDED("852"),



    TRANSACTION_ABORTED("891"),
    TRANSACTION_REQUEST_NOT_FOUND("892"),

    LICENSE_EXPIRED("893"),


    ;

    private static final Map<String, ResponseCode> codeMap = new HashMap<>();

    static {
        for (ResponseCode e : values()) {
            codeMap.put(e.type, e);
        }
    }

     public final String type;

    ResponseCode(String type)
    {
        this.type = type;
    }

    @JsonCreator
    public static ResponseCode valueOfType(String label)
    {
        label = label.toUpperCase();
        return codeMap.get(label);
    }
//    public static CoopResponseCode valueOfCode(String code)
//    {
//        for (CoopResponseCode e : values()) {
//            if (e.code.equals(code)) {
//                return e;
//            }
//        }
//        return null;
//    }
}
