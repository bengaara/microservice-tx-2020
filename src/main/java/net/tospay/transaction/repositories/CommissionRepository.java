package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.tospay.transaction.entities.Commission;
import net.tospay.transaction.entities.Reversal;
import net.tospay.transaction.enums.MakerCheckerStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepository extends BaseRepositoryInterface<Commission, UUID> {

    @Query(value =
        "select * from commission r where id = :uuid", nativeQuery = true)
    Optional<Commission> findById(UUID uuid); //same id//Optional<Reversal> findById(UUID uuid);


}
