package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.tospay.transaction.entities.Reversal;
import net.tospay.transaction.enums.MakerCheckerStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.stereotype.Repository;

@Repository
public interface ReversalRepository extends BaseRepositoryInterface<Reversal, UUID> {

    @Query(value =
        "select * from reversal r where id = :uuid", nativeQuery = true)
    Optional<Reversal> findById(UUID uuid); //same id//Optional<Reversal> findById(UUID uuid);

    @Query(value =
        "select r.* from reversal r left join transactions t on t.id=r.transaction  where t.transaction_id = :transactionId", nativeQuery = true)
    Optional<Reversal> findByTransactionId(String transactionId);

    @Query(value =
        "select * from reversal r where true and " +
            //no owner (:userId is null or r.maker->>'user_id' =:userId) and
            " (coalesce(:mc_status,null) is null or  r.mc_status LIKE %:mc_status% ) and (coalesce(:checkerStage, null)  is null or jsonb_array_length(r.checker_records) =:checkerStage )  and r.date_created between :from  and :to", nativeQuery = true)
    List<Reversal> findReversal( MakerCheckerStatus mc_status,  Integer checkerStage, LocalDateTime from, LocalDateTime to, Pageable p);

}
