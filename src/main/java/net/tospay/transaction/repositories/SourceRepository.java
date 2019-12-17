package net.tospay.transaction.repositories;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.NamedNativeQuery;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.UserType;

@Repository
public interface SourceRepository extends BaseRepositoryInterface<Source, UUID>
{
    Optional<Source> findById(UUID uuid);

     @Query(value = "select * from sources where payload ->'account'->>'user_id' = :userId",nativeQuery = true)
    ArrayList< Source> findByUserId(String userId,Pageable p);
}
