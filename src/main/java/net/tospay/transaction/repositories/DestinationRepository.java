package net.tospay.transaction.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.enums.AccountType;

@Repository
public interface DestinationRepository extends BaseRepositoryInterface<Destination, UUID>
{
    Optional<Destination> findById(UUID uuid);

    ArrayList<Destination> findByUserIdAndUserType(UUID userId, AccountType userType, Pageable pageable);


}
