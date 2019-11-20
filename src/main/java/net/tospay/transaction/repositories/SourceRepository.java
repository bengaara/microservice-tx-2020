package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Source;

@Repository
public interface SourceRepository extends BaseRepositoryInterface<Source, UUID>
{
    Optional<Source> findById(UUID uuid);
}
