package net.tospay.transaction.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.enums.AccountType;

@Repository
public interface DestinationRepository extends BaseRepositoryInterface<Destination, UUID>
{
    Optional<Destination> findById(UUID uuid);

    @Query(value = "SELECT d FROM Destination d " +
            "WHERE d.userId=:userId and d.userType=:userType ")
    ArrayList<Destination> fetchByUserIdAndUserType(UUID userId, AccountType userType);
}
