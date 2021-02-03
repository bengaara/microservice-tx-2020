package net.tospay.transaction.repositories;

import java.util.Optional;
import java.util.UUID;
import net.tospay.transaction.entities.License;
import net.tospay.transaction.entities.Location;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends BaseRepositoryInterface<Location, UUID> {
    @Override
    Optional<Location> findById(UUID uuid);

    Optional<Location> findFirstByCellid(String cellid);


}
