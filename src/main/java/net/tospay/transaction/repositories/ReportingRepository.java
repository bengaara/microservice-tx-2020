package net.tospay.transaction.repositories;

import net.tospay.transaction.entities.Report;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportingRepository extends BaseRepositoryInterface<Report, UUID> {
    @Override
    Optional<Report> findById(UUID uuid);

    Optional<Report> findFirstByOrderByDateCreatedDesc();

    Optional<Report> findFirstByUserIdOrderByDateCreatedDesc(UUID userId);

    Optional<Report> findFirstByUserIdAndDateFromAndDateTo(UUID userId, LocalDateTime dateFrom, LocalDateTime dateTo);


}
