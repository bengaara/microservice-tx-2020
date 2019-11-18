package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Source;

@Repository
public interface SourceRepository extends PagingAndSortingRepository<Source, UUID>
{
    Optional<Source> fetchDistinctById(UUID uuid);

    Optional<Source> findDistinctByTransactionId(String transactionId);
}
