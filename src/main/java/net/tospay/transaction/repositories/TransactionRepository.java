package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.enums.TransactionStatus;

@Repository
public interface TransactionRepository extends BaseRepositoryInterface<net.tospay.transaction.entities.Transaction, UUID>
{
    Optional<net.tospay.transaction.entities.Transaction> findById(UUID uuid);

    Optional<net.tospay.transaction.entities.Transaction> findByTransactionId(String transactionId);

    @Query(value = "from Transaction where status LIKE %:status%  and date_modified  >:dateFrom ")
    List<net.tospay.transaction.entities.Transaction> findByStatusAndDate( TransactionStatus status, LocalDateTime dateFrom);
}
