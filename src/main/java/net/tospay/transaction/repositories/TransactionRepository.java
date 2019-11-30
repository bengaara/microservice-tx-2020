package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.Transfer;

@Repository
public interface TransactionRepository extends BaseRepositoryInterface<Transaction, UUID>
{
    Optional<Transaction> findById(UUID uuid);

    Optional<Transaction> findByTransactionId(String transactionId);

    @Query(value = "from Transaction where status LIKE %:status%  and date_modified  >:dateFrom ")
    List<Transaction> findByStatusAndDate( Transfer.TransactionStatus status, LocalDateTime dateFrom);
}
