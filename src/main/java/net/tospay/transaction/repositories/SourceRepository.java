package net.tospay.transaction.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;

@Repository
public interface SourceRepository extends BaseRepositoryInterface<Source, UUID>
{
    Optional<Source> findById(UUID uuid);
    ArrayList< Source> findByUserIdAndUserType(UUID userId, AccountType userType, Pageable p);
}
