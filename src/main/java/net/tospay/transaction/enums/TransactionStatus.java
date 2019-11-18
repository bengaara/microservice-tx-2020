package net.tospay.transaction.enums;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 7/22/2019, Mon
 **/
public enum TransactionStatus
{
    CREATED(0),
    PROCESSING(1),
    FAIL(2),
    SUCCESS(3);

    private int transactionStatus;

    TransactionStatus(int transactionStatus)
    {
        this.transactionStatus = transactionStatus;
    }

    public int getTransactionStatus()
    {
        return transactionStatus;
    }
}
