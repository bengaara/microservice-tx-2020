package net.tospay.transaction.repositories;

import net.tospay.transaction.entities.License;
import net.tospay.transaction.entities.Report;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseRepository extends BaseRepositoryInterface<License, UUID> {
    @Override
    Optional<License> findById(UUID uuid);

    Optional<License> findFirstByOrderByDateCreatedDesc();


}
