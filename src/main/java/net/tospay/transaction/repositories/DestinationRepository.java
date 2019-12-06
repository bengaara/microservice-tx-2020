package net.tospay.transaction.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.enums.UserType;

@Repository
public interface DestinationRepository extends BaseRepositoryInterface<Destination, UUID>
{
    Optional<Destination> findById(UUID uuid);

    @Query(value = "select * from destinations where payload ->'account'->'user_d' like  %:userId%",nativeQuery = true)
    ArrayList<Destination> findByUserId(UUID userId, Pageable pageable);


}
