package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Destination;

@Repository
public interface DestinationRepository extends PagingAndSortingRepository<Destination, UUID>
{
    Optional<Destination> fetchDistinctById(UUID uuid);
}
