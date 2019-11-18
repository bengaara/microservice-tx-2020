package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Transaction;

@Repository
public interface TransactionRepository extends PagingAndSortingRepository<Transaction, UUID>
{
    Optional<Transaction> fetchDistinctById(UUID uuid);

    Optional<Transaction> findDistinctByTransactionId(String transactionId);
}
