package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;
import net.tospay.transaction.entities.TransactionConfig;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionConfigRepository extends
    BaseRepositoryInterface<TransactionConfig, UUID> {

    Optional<TransactionConfig> findById(UUID uuid);


    @Query(value = "select * from transaction_config order by date_created DESC limit 1", nativeQuery = true)
    Optional<TransactionConfig> findLatestConfig();

}
