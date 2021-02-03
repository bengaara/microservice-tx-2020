package net.tospay.transaction.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.tospay.transaction.entities.TransactionInit;
import net.tospay.transaction.enums.MakerCheckerStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionInitRepository extends
    BaseRepositoryInterface<TransactionInit, UUID> {

    Optional<TransactionInit> findById(UUID uuid);

    Optional<TransactionInit> findByChildTransaction(UUID uuid);

    @Query(value =
        "select * from transaction_init t where true " +
            " and ((coalesce(:agentId,null) is null or t.user_info->>'agent_id' is null) or t.user_info->>'agent_id' = :agentId ) and ( t.mc_status LIKE %:mcStatus% ) and ( jsonb_array_length(t.checker_records) =:checkerStage ) and t.date_created between :from  and :to", nativeQuery = true)
    List<TransactionInit> findTransactionInit(UUID agentId, MakerCheckerStatus mcStatus,
        Integer checkerStage, LocalDateTime from, LocalDateTime to, Pageable p);


//    @Query(value =
//        "select * from transaction_init t where true " +
//            " and ( NULLIF(t.user_info->>'agent_id','') is null) and (coalesce(:mc_status,null) is null or t.mc_status =:mc_status ) and (coalesce(:checkerStage, null)  is null or jsonb_array_length(t.checker_records) =:checkerStage ) and t.date_created between :from  and :to", nativeQuery = true)
//    List<TransactionInit> findSystemTransactionInit( MakerCheckerStatus mc_status,Integer checkerStage, LocalDateTime from, LocalDateTime to, Pageable p);

    @Query(value = "select * from transaction_init where  payload->'fraudInfo'->>'fraudQuery' = :fraudQuery ", nativeQuery = true)
    Optional<TransactionInit> findByFraudReference(String fraudQuery);

}
