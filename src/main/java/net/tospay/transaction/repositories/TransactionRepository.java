package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;

@Repository
public interface TransactionRepository extends BaseRepositoryInterface<net.tospay.transaction.entities.Transaction, UUID>
{
    Optional<net.tospay.transaction.entities.Transaction> findById(UUID uuid);

    Optional<net.tospay.transaction.entities.Transaction> findByTransactionId(String transactionId);

    @Query(value = "from Transaction where status LIKE %:status%  and dateModified  >:dateFrom and refundRetryCount <= :refundRetryCount")
    List<Transaction> findByStatusAndDateAndRefundRetryCountLimit( TransactionStatus status, LocalDateTime dateFrom,int refundRetryCount);

    @Query(value = "select distinct t from Transaction t left join Source s on s.transaction=t.id where s.transactionStatus like 'SUCCESS'  and t.transactionStatus = :status  and t.dateModified  >:dateFrom and t.refundRetryCount <= :refundRetryCount")
    List<Transaction> findByStatusAndDateAndRefundRetryCountLimitAndSourceStatus( TransactionStatus status, LocalDateTime dateFrom,int refundRetryCount);


    @Query(value = "select * from transactions where user_info ->'userId' like  %:userId%",nativeQuery = true)
    ArrayList<Transaction> findByUserInfoUserId(UUID userId,  Pageable p);
}
