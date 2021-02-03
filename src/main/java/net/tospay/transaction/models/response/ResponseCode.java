package net.tospay.transaction.models.response;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 7/16/2019, Tue
 **/
public enum ResponseCode
{
    SUCCESS("000"),
    GENERAL_FAILURE("200"),
    INVALID_BANK_TOKEN("201"),
    INVALID_REQUEST("202"),
    INVALID_TRANSACTION_ID("203"),
    UNSUPPORTED_BANK("204"),
    NO_ACCOUNTS_FOUND("205"),
    DUPLICATE_ENTRY("206"),
    NOT_SUPPORTED_BY_PROVIDER("207"),
    TRANSACTION_FAILED("208"),
    INVALID_UUID_FROM_STRING("209"),
    INVALID_SESSION_TOKEN("210"),
    INSUFFICIENT_FUNDS("211"),
    BANK_FAILURE("299");

    public final String responseCode;

    ResponseCode(String responseCode)
    {
        this.responseCode = responseCode;
    }
}
