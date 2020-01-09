package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Report;

@Repository
public interface ReportingRepository extends BaseRepositoryInterface<Report, UUID>
{
    Optional<Report> findById(UUID uuid);

    Optional<Report> findFirstByOrderByDateCreatedDesc();

    Optional<Report> findFirstByUserIdOrderByDateCreatedDesc(UUID userId);
}
