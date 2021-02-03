package net.tospay.transaction.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.tospay.transaction.entities.Revenue;
import net.tospay.transaction.entities.SourceDestinationMerge;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends
    BaseRepositoryInterface<net.tospay.transaction.entities.Transaction, UUID> {

    @Override
    Optional<net.tospay.transaction.entities.Transaction> findById(UUID uuid);

    @Query(value = "select * from transactions where  transaction_id = :transactionId", nativeQuery = true)
    Optional<net.tospay.transaction.entities.Transaction> findByTransactionId(String transactionId);

    @Query(value = "select * from transactions where  payload->'orderInfo'->>'reference' = :reference  and (:userId is null or  payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id'= :userId) order by date_created DESC limit 1", nativeQuery = true)
    List<net.tospay.transaction.entities.Transaction> findByReferenceAndPartnerId(String reference,
        @Nullable UUID userId);

    @Query(value = "select * from transactions where  payload->'orderInfo'->>'reference' = :reference order by date_created DESC", nativeQuery = true)
    List<net.tospay.transaction.entities.Transaction> findByReference(String reference);

    @Query(value = "select * from transactions where  payload->'fraudInfo'->>'fraudQuery' = :fraudQuery and type !='REVERSAL' order by date_created desc limit 1", nativeQuery = true)
    Optional<net.tospay.transaction.entities.Transaction> findByFraudReference(String fraudQuery);

    @Query(value = "select * from transactions where  payload->'orderInfo'->>'payment_id' = :paymentId and type !='REVERSAL'", nativeQuery = true)
    Optional<net.tospay.transaction.entities.Transaction> findByPaymentId(String paymentId);


    @Query(value = "select * from transactions where status\\:\\:string  = :status and date_modified  <:from", nativeQuery = true)
    List<Transaction> findByStatusBefore(TransactionStatus status, LocalDateTime from);

    @Query(value = "select * from transactions where status\\:\\:string  = :status and type\\:\\:string = :type  and date_modified  <:from", nativeQuery = true)
    List<Transaction> findByStatusAndTypeBefore(TransactionStatus status,TransactionType type, LocalDateTime from);

    List<Transaction> findByTransactionStatusNotAndTransactionIdIsNull(TransactionStatus status);

    @Query(value = "select distinct t.* from transactions t left join sources s on s.transaction=t.id where s.status\\:\\:string ='SUCCESS'  and t.status\\:\\:string = 'FAILED' and t.type !='REVERSAL'  and t.date_modified  >:from  and t.reversed = false", nativeQuery = true)
    List<Transaction> findSourcedFailedUnreversedTransactions(LocalDateTime from);

    List<Transaction> findAllByDateCreatedIsBetween(LocalDateTime from, LocalDateTime to, Pageable p);

    //   List<Transaction> findAllByPublishedAccountInfoIsFalse(Pageable p);



    @Query(value =
        "select * from transactions t where " +
            " (t.id like  %::id% or t.transaction_id like  %::id% or payload->'orderInfo'->>'reference' like %:id% payload->'orderInfo'->>'payment_id' like %:id%)  and d.date_created between :from  and :to", nativeQuery = true)
    List<Transaction> findByTransactionLike(String id, LocalDateTime from, LocalDateTime to, Pageable p);

    @Query(value =
        "select * from transactions t where id in (\n"
            + "(select transaction from sources s where (s.account_id  =:msisdn or s.account_user_id =:msisdn or s.account_phone =:msisdn ) and (s.status='SUCCESS' or s.status='FAILED' ) and s.date_created between :from  and :to)\n"
            + "union \n"
            + "(select transaction from destinations d where ( d.account_id =:msisdn or d.account_user_id =:msisdn or d.account_phone =:msisdn) and (d.status = 'SUCCESS')\n"
            + "and d.date_created between :from  and :to )\n"
            + ")", nativeQuery = true)
    List<Transaction> findByMsisdn(String msisdn, LocalDateTime from, LocalDateTime to,
        Pageable p);


//    @Query(value =
//        "select * from transactions t left join sources s  on s.transaction=t.id where " +
//            " (s.payload->'account'->>'user_id' =:userId) and (s.status = 'SUCCESS' OR s.status = 'REVERSED'  OR s.status = 'FAILED') and d.date_created between :from  and :to", nativeQuery = true)
//    List<Transaction> findBySourceUserId(UUID userId, LocalDateTime from, LocalDateTime to,
//        Pageable p);
//
//    //fetch successful deliveries only
//    @Query(value =
//        "select * from transactions t left join destinations d  on d.transaction=t.id left join sources s  on s.transaction=t.id where "
//            +
//            "s.payload->'account'->>'user_id' != d.payload->'account'->>'user_id' " +
//            "and (d.payload->'account'->>'user_id' =:userId) and (d.status = 'SUCCESS' OR d.status = 'REVERSED' ) and d.date_created between :from  and :to", nativeQuery = true)
//    List<Transaction> findByDeliverUserIdNotInSourceUserId(UUID userId, LocalDateTime from,
//        LocalDateTime to, Pageable p);

    @Query(value = "select * from transactions where payload ->'chargeInfo'->'partnerInfo'->'account'->>'user_id' like  %:userId% and date_created between :from  and :to ", nativeQuery = true)
    List<Transaction> findByPartnerId(UUID userId, LocalDateTime from, LocalDateTime to, Pageable pageable);

    @Query(value = "select * from transactions where (:partnerId is null or and payload ->'chargeInfo'->'partnerInfo'->'account'->>'user_id' =:partnerId) (:userId is null or and user_info->>'user_id' =:userId)", nativeQuery = true)
    List<Transaction> findByPartnerIdAndUserId(UUID partnerId, UUID userId, Pageable pageable);

    @Query(value = "select count(*) as records,date_trunc('day', date_created) as created,type,\n" +
            "sum(railRevenue) AS railRevenue,sum(partnerRevenue) AS partnerRevenue from \n" +
            "((select t.id,t.type,t.date_created,t.user_info->>'user_id' as userId\n" +
            "   ,(t.payload->'chargeInfo'->'railInfo'->'amount'->>'amount')\\:\\:float8 as railRevenue\n" +
            "   ,(t.payload->'chargeInfo'->'partnerInfo'->'amount'->>'amount')\\:\\:float8 as partnerRevenue\n" +
            "   ,(t.payload->'partnerInfo'->'account'->>'user_id') as partnerId\n" +
            "from transactions t \n" +
            " where t.date_created between :from  and :to" +
            " and (:userId is null or t.user_info->>'user_id' =:userId)) \n" +
            "union \n" +
            "(select t.id,t.type,t.date_created,d.payload->'account'->>'user_id' as userId \n" +
        "   ,(t.payload->'chargeInfo'->'railInfo'->'amount'->>'amount')\\:\\:float8 as railRevenue\n"
        +
        "   ,(t.payload->'chargeInfo'->'partnerInfo'->'amount'->>'amount')\\:\\:float8 as partnerRevenue\n"
        +
        "   ,(t.payload->'partnerInfo'->'account'->>'user_id') as partnerId\n" +
        "from transactions t left join destinations d  on d.transaction=t.id \n" +
        " \n" +
        "where\n" +
        "t.user_info->>'user_id' != d.payload->'account'->>'user_id'\n" +
        "           and t.date_created between :from  and :to and t.id not in (select t.id from transactions t   "
        +
        " where t.date_created between :from  and :to ) and (:userId is null or d.payload->'account'->>'user_id' =:userId) ) )"
        +
        "  group by date_trunc('day', date_created) ,type order by type,created desc", nativeQuery = true)
    ArrayList<TransactionSummary> findWeeklyTransactionByUserId(UUID userId, LocalDateTime from,
        LocalDateTime to);

    @Query(value = "select count(*) as records,date_trunc('day', date_created) as created,type,\n" +
        "sum(railRevenue) AS railRevenue,sum(partnerRevenue) AS partnerRevenue,currency from \n"
        +
        "(select t.id,t.type,t.date_created,t.user_info->>'user_id' as userId\n" +
        "   ,(t.payload->'chargeInfo'->'railInfo'->'amount'->>'amount')\\:\\:float8 as railRevenue\n"
        +
        "   ,(t.payload->'chargeInfo'->'partnerInfo'->'amount'->>'amount')\\:\\:float8 as partnerRevenue\n"
        +
        "   ,(t.payload->'chargeInfo'->'partnerInfo'->'amount'->>'currency') as currency\n"
        +
        "   ,(t.payload->'partnerInfo'->'account'->>'user_id') as partnerId\n" +
        "from transactions t left join sources s on s.transaction=t.id left join destinations d on d.transaction=t.id \n" +

        " where t.date_created between :from  and :to" +
        " and (:partnerId is null or t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' =:partnerId)"
        +"and (:msisdn\\:\\:text is null or  s.account_id  =:msisdn or s.account_user_id =:msisdn or s.account_phone =:msisdn )"
        +"and (:msisdn\\:\\:text is null or  d.account_id  =:msisdn or d.account_user_id =:msisdn or d.account_phone =:msisdn )"
        + "and (:currency\\:\\:text is null or t.payload->'chargeInfo'->'partnerInfo'->'amount'->>'currency' =:currency) )"
        +
        "  group by date_trunc('day', date_created) ,type,currency order by type,created desc", nativeQuery = true)
    ArrayList<TransactionSummary> findTransactionByPartnerId(UUID partnerId,String msisdn,String currency,
        LocalDateTime from, LocalDateTime to);

    @Query(value =
        "select t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' from transactions t where t.date_created between :from  and :to\n"
            +
            "             and (:userId is null or t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' =:userId) \n"
            +
            "             group by  t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id'", nativeQuery = true)
    List<UUID> findPartnerWithTransactionInRange(UUID userId, LocalDateTime from,
        LocalDateTime to);

    @Query(value = "select t.payload->'chargeInfo'->'partnerInfo'->'account' from transactions t where t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' =:userId order by date_created desc limit 1", nativeQuery = true)
    Object findPartnerAccount(UUID userId);

    @Query(value = "select * from(select  unnest(array[a,b]) as user_id from (select json_array_elements(t.payload->'delivery')->'account'->>'user_id' as a ,t.user_info ->>'user_id' as b from transactions t) where t.date_created between :from  and :to)\n" +
            ") group by user_id", nativeQuery = true)
    List<UUID> findUserWithTransactionInRange(LocalDateTime from, LocalDateTime to);

    @Query(value = "select * from (select t.id,json_array_elements(t.payload->'source') as source,json_array_elements(t.payload->'delivery') as destination ,t.user_info ->>'user_id' as user_info from transactions t  where t.date_created between :from  and :to) as t where source->'account'->>'user_id' = :userId OR destination->'account'->>'user_id' = :userId", nativeQuery = true)
    List<SourceDestinationMerge> findUserTransactionInRange(UUID userId, LocalDateTime from,
        LocalDateTime to);


    @Query(value =
            "select partner_id,currency\n" +
                    ",count(day) as items\n" +
                    "--, SUM(amount::float) FILTER (WHERE type='TOPUP') AS t\n" +
                    "--, SUM(amount::float) FILTER (WHERE type='TRANSFER') AS tr\n" +
                    ",sum(TOPUP\\:\\:float) as  TOPUP\n" +
                    ",sum(TRANSFER\\:\\:float) as  TRANSFER\n" +
                    ",sum(WITHDRAW\\:\\:float) as  WITHDRAW\n" +
                    ",sum(REVERSAL\\:\\:float) as  REVERSAL\n" +
                    ",sum(SETTLEMENT\\:\\:floaT) as  SETTLEMENT\n" +
                    ",sum(PAYMENT\\:\\:float) as  PAYMENT\n" +
                    ",sum(UTILITY\\:\\:float) as  UTILITY\n" +
                    ",day,EXTRACT(week FROM week) as weeks,week,EXTRACT(month FROM month) as months,month,EXTRACT(year FROM year) as years,year\n" +
                    // " , :groupType,EXTRACT(:groupType FROM :groupType) as groupType " +

                    "\n" +
                    "from \n" +
                    "(\n" +
                    "select \n" +
                    "case when :groupType = 'DAY' then  date_trunc('day' , d.date_created) else null end as day ,\n" +
                    "case when :groupType = 'WEEK' OR :groupType = 'DAY' then date_trunc('week' , d.date_created) else null end as week, --EXTRACT(DAY FROM  d.date_created) as day ,EXTRACT(WEEK FROM  d.date_created) as week,\n" +
                    "case when :groupType = 'MONTH' OR  :groupType = 'WEEK'  OR :groupType = 'DAY' then date_trunc('month' , d.date_created) else null end as month, --EXTRACT(MONTH FROM  d.date_created) as mon,\n" +
                    "case when :groupType = 'YEAR'  OR :groupType = 'MONTH'  OR :groupType = 'WEEK'  OR :groupType = 'DAY' then date_trunc('year' , d.date_created) else null end as year, \n" +
                    "t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' as partner_id,\n" +
                    "t.type,\n" +
                    "(case when t.type='TOPUP' then d.payload->'total'->>'amount' end) as  TOPUP,\n" +
                    "(case when t.type='TRANSFER' then d.payload->'total'->>'amount' end) as  TRANSFER,\n" +
                    "(case when t.type='WITHDRAW' then d.payload->'total'->>'amount' end) as  WITHDRAW,\n" +
                    "(case when t.type='REVERSAL' then d.payload->'total'->>'amount' end) as  REVERSAL,\n" +
                    "(case when t.type='SETTLEMENT' then d.payload->'total'->>'amount' end) as  SETTLEMENT,\n" +
                    "(case when t.type='PAYMENT' then d.payload->'total'->>'amount' end) as  PAYMENT,\n" +
                    "(case when t.type='UTILITY' then d.payload->'total'->>'amount' end) as  UTILITY,\n" +
                    "d.revenue,\n" +
                    "d.id,d.date_created ,d.status,d.transaction,d.payload->'account'->>'user_id' as user_id,d.payload->'total'->>'amount' as amount,d.payload->'total'->>'currency' as currency from destinations d\n" +
                    "left join transactions t on d.transaction =t.id \n" +
                    "left join transactions t1 on t.id=t1.parent_id \n" +
                    "where\n" +
                    //"d.revenue =true and\n" +
                    "t.status ='SUCCESS' and (t1.type !='REVERSAL' || t1.type is null)\n" +
                    "  and  t.payload->'chargeInfo'->'partnerInfo'->'account'->>'user_id' = :partnerId " +
                "and d.date_created between :from  and :to " +
                "order by d.date_created desc \n" +
                "\n" +
                "\n" +
                ")\n" +
                "group by partner_id,currency,day,week,month,year\n" +
                // ", :groupType \n" +
                "order by day,week,month,year desc\n" +
                // "order by case when :groupType = 'DAY' then day when :groupType = 'WEEK' then week when :groupType = 'MONTH' then month when :groupType = 'YEAR' then year end" +
                " limit  :limit offset  :offset ", nativeQuery = true)
    List<Revenue> findRevenueByPartnerId(UUID partnerId, LocalDateTime from, LocalDateTime to,
        Integer limit, Integer offset, String groupType);//DashboardRequest.GroupType


    @Projection(name = "AccountDetail", types = AccountDetail.class)
    interface AccountDetail {

        UUID getUserId();

        UserType getUserType();

        String getType();

        String getAccountNo();

        String getEmail();
    }

    @Projection(name = "TransactionSummary", types = TransactionSummary.class)
    interface TransactionSummary {

        BigDecimal getRecords();

        Date getCreated();

        TransactionType getType();

        UUID getUserId();

        UUID getPartnerId();

        BigDecimal getFxRevenue();

        BigDecimal getPartnerRevenue();

        BigDecimal getRailRevenue();

        String getCurrency();
    }

}
