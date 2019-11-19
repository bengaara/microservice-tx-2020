package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Destination;

@Repository
public interface DestinationRepository extends CrudRepository<Destination, UUID>
{
    Optional<Destination> findById(UUID uuid);
}
