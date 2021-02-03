package net.tospay.transaction.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import net.tospay.transaction.entities.BaseEntity;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Reversal;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.entities.TransactionInit;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.response.DashboardTransactionSummary;
import net.tospay.transaction.models.response.StatementItem;
import net.tospay.transaction.models.response.StatementResponse;
import net.tospay.transaction.models.response.SummaryStatement;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.models.response.TransactionNode;
import net.tospay.transaction.models.response.UserInfoStatement;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.OffsetBasedPageRequest;
import net.tospay.transaction.repositories.ReversalRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionInitRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DashboardService extends BaseService {

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    TransactionRepository transactionRepository;

    TransactionInitRepository transactionInitRepository;

    ReversalRepository reversalRepository;

    public DashboardService(SourceRepository sourceRepository,
        DestinationRepository destinationRepository,
        TransactionRepository transactionRepository,
        TransactionInitRepository transactionInitRepository,
        ReversalRepository reversalRepository) {
        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
        this.transactionRepository = transactionRepository;
        this.transactionInitRepository = transactionInitRepository;
        this.reversalRepository = reversalRepository;
    }

//    public @NotNull List<Transaction> fetchInboundTransaction(UUID userId, Integer offset,
//        Integer limit, LocalDateTime from, LocalDateTime to) {
//        try {
//            logger.info("fetchTransactionInBound {} {} {}", userId, from, to);
//
//            LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from;
//            LocalDateTime t = to == null ? LocalDateTime.now() : to;
//
//
//            return transactionRepository.findByDeliverUserIdNotInSourceUserId(userId, f, t,
//                    new OffsetBasedPageRequest(offset, limit,
//                            Sort.by(Transaction.DATE_CREATED).descending()));//BaseEntity.toDbField(
//        } catch (Exception e) {
//            logger.error("", e);
//            return new ArrayList<Transaction>();
//        }
//    }

    public List<TransactionFetchResponse> fetchAllTransaction(Integer offset, Integer limit,
        LocalDate from, LocalDate to) {
        try {
            logger.info(" {} {} {} {}", offset, limit, from, to);

          LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
          LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();

            List<Transaction> list = transactionRepository.findAllByDateCreatedIsBetween(f, t,
                new OffsetBasedPageRequest(offset, limit,
                    Sort.by(BaseEntity.toDbField(Transaction.DATE_CREATED)).descending()));

            return filter(UserType.ADMIN, null, list);


        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<TransactionFetchResponse>();
        }
    }


    //    public List<Transaction> fetchOutboundTransaction(UUID userId, Integer offset, Integer limit, LocalDateTime from, LocalDateTime to) {
//        try {
//            logger.info("fetchOutboundTransaction {} {} {}", userId, from, to);
//
//            LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from;
//            LocalDateTime t = to == null ? LocalDateTime.now() : to;
//
//
//            return transactionRepository.findBySourceUserId(userId, f, t,
//                    new OffsetBasedPageRequest(offset, limit,
//                            Sort.by(Transaction.DATE_CREATED).descending()));
//        } catch (Exception e) {
//            logger.error("", e);
//            return new ArrayList<Transaction>();
//        }
//    }

     List<Transaction> fetchTransactionByMsisdn(String msisdn, Integer offset, Integer limit,
        LocalDateTime from, LocalDateTime to) {
        try {
            logger.info("fetchTransactionByMsisdn {} {} {}", msisdn, from, to);

            LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from;
            LocalDateTime t = to == null ? LocalDateTime.now() : to;

            return transactionRepository.findByMsisdn(msisdn, f, t,
                new OffsetBasedPageRequest(offset, limit,
                    Sort.by(Transaction.DATE_CREATED).descending()));
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Transaction>();
        }
    }

    public @NotNull List<TransactionFetchResponse> fetchFilteredTransaction(UserType userType, String userId, Integer offset, Integer limit, LocalDate from, LocalDate to) {
        logger.debug("fetchFilteredTransaction {} {} {} {} {} {}", userId, userType, offset,
            limit, from, to);

        LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
        LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();

//        List<Transaction> list1 = fetchOutboundTransaction(userId, offset, limit, f, t);
//        List<Transaction> list2 = fetchInboundTransaction(userId, offset, limit, f, t);
        List<Transaction> list = fetchTransactionByMsisdn(userId, offset, limit, f, t);
        //for list 2 from external partner, remove

        return filter(userType, userId, list);
    }

    public @NotNull StatementResponse createStatement( String ownerId, LocalDate from, LocalDate to, List<TransactionFetchResponse> list) {
      logger.debug("createStatement tx items: {} {} {} {} {} {}", list != null ? list.size() : 0);

      UserInfoStatement customer = new UserInfoStatement();
      outer:
      for (TransactionFetchResponse tx : list) {
        if (ownerId.equalsIgnoreCase(Objects.toString(tx.getUserId())) || ownerId
            .equalsIgnoreCase(Objects.toString(tx.getId())) || ownerId
            .equalsIgnoreCase(Objects.toString(tx.getPhone()))) {
          logger.debug("customer in source  {} {}  {} {}", ownerId, tx.getUserId());
          customer.setName(tx.getName());
          customer.setEmail(tx.getEmail());
          customer.setPhone(tx.getPhone());
          customer.setUserId(tx.getUserId());
          customer.setTypeId(tx.getUserType());
          break;
        }
        for (TransactionNode des : tx.getDestination()) {
          if (ownerId.equalsIgnoreCase(Objects.toString(des.getUserId())) || ownerId
              .equalsIgnoreCase(Objects.toString(des.getId())) || ownerId
              .equalsIgnoreCase(Objects.toString(des.getPhone()))) {
            logger.debug("customer in destination  {} {}  {} {}", ownerId,
                des.getUserId());
            customer.setName(des.getName());
            customer.setEmail(des.getEmail());
            customer.setPhone(des.getPhone());
            customer.setUserId(des.getUserId());
            customer.setTypeId(des.getType());
            break outer;
          }

        }

      }

      StatementResponse statementResponse = new StatementResponse();

        customer.setDate(LocalDateTime.now());

        customer.setFrom(from);
        customer.setTo(to);
        if(from ==null || to ==null && list.size()>0){
          customer.setFrom(list.get(list.size()-1).getDateCreated().toLocalDate());
          customer.setTo(list.get(0).getDateCreated().toLocalDate());
        }


        statementResponse.setCustomer(customer);



        AtomicReference<BigDecimal> sent = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> received = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalIncoming = new AtomicReference<>(BigDecimal.ZERO);
        AtomicReference<BigDecimal> totalOutgoing = new AtomicReference<>(BigDecimal.ZERO);
        list.stream().forEach(transactionFetchResponse -> {
            StatementItem item = StatementItem.from(customer.getUserId(),transactionFetchResponse);
            statementResponse.getItems().add(item);
            if (item.incoming != null) {
                received.set(received.get().add(BigDecimal.valueOf(item.incoming.floatValue())));
            }
            if (item.outgoing != null) {
                sent.set(sent.get().add(BigDecimal.valueOf(item.outgoing.floatValue())));
            }
        });
        totalIncoming.set(received.get());
        totalOutgoing.set(sent.get());
        SummaryStatement summaryStatement = new SummaryStatement();
        summaryStatement.received = received.get();
        summaryStatement.sent = sent.get();
        summaryStatement.totalIncoming = totalIncoming.get();
        summaryStatement.totalOutgoing = totalOutgoing.get();

        statementResponse.setSummaryStatement(summaryStatement);
        return statementResponse;
    }

   public List<TransactionFetchResponse> filter(UserType userType, String userId, List<Transaction> list) {

        try {
            List<TransactionFetchResponse> listUI = null;
            logger.debug("filter  {} {} sourceList: {} {}", userType, userId,
                list.size());
            listUI = list.stream().map(transaction -> {
                for (Iterator<Destination> iterator = transaction.getDestinations().iterator();
                    iterator.hasNext(); ) {
                    Destination d = iterator.next();
                    if (userId != null && !transaction.getUserInfo().getUserId().equals(userId)
                        && (!userId.equals(d.getPayload().getAccount().getUserId().toString())&& !userId.equals(d.getPayload().getAccount().getPhone()))) {
                        // Remove the current element from the iterator and the list.
                        logger.debug("filter out destinations:  {} not meant for user: {}",
                            d.getId(), userId);
                        iterator.remove();
                    }
                }
                return transaction;
            }).map(TransactionFetchResponse::from).map(transactionFetchResponse -> {
                // logger.debug("filter out all revenue from {}", userType);
                if (UserType.ADMIN.equals(userType)) {//admin sees all

                } else if (UserType.PARTNER
                    .equals(userType)) {//filter out all revenue from user view
                    transactionFetchResponse.setRailRevenue(null);

                } else {//filter out all revenue from rest: users/agents/merchant
                    transactionFetchResponse.setRailRevenue(null);
                    transactionFetchResponse.setPartnerRevenue(null);

                }
                return transactionFetchResponse;
            }).collect(Collectors.toList());

            return listUI;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<TransactionFetchResponse>();
        }
    }

    public @NotNull List<Source> fetchSources(UUID userId, Integer offset, Integer limit) {
        try {

            logger.info(" {}", userId);
            //   Sort.TypedSort<Source> s=Sort.sort(Source.class);
            //   Sort sort = s.by(Source::getDateModified).descending();
            if (userId == null) {
                Pageable p = new OffsetBasedPageRequest(offset, limit, Sort.by(Source.DATE_CREATED).descending());
                Page page = sourceRepository.findAll(p);
                return page.getContent();
            } else {
                return sourceRepository.findByUserId(userId,
                        new OffsetBasedPageRequest(offset, limit,
                                Sort.by(Source.DATE_CREATED).descending()));
            }

        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Source>();
        }
    }


    public @NotNull List<Destination> fetchDestinations(UUID userId, Integer offset, Integer limit) {
        try {
            logger.info(" {}", userId);
            if (userId == null) {
                Pageable p = new OffsetBasedPageRequest(offset, limit, Sort.by(Source.DATE_CREATED).descending());
                Page page = destinationRepository.findAll(p);
                return page.getContent();
            } else {
                return destinationRepository.findByUserId(userId,
                        new OffsetBasedPageRequest(offset, limit,
                                Sort.by(Source.DATE_CREATED).descending()));
            }
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Destination>();
        }
    }



    public @NotNull Optional<Transaction> fetchTransactionByTransactionId(
            String transactionId) {
        try {
            logger.info(" {}", transactionId);
            return transactionRepository.findByTransactionId(transactionId);
        } catch (Exception e) {
            logger.error("", e);
            return Optional.empty();
        }
    }

    public @NotNull Optional<Transaction> fetchTransactionById(UUID id) {
        try {
            logger.info(" {}", id);
            return transactionRepository.findById(id);
        } catch (Exception e) {
            logger.error("", e);
            return Optional.empty();
        }
    }


    public @NotNull List<TransactionFetchResponse> fetchPartnerTransaction(UUID userId, Integer offset, Integer limit, LocalDate from, LocalDate to) {
        try {
            logger.info(" {} {} {} {} {}", userId,offset,limit, from,to);

            LocalDateTime f = from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
            LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();


            List<Transaction> l = transactionRepository.findByPartnerId(userId,f,t,
                new OffsetBasedPageRequest(offset, limit,
                    Sort.by(Destination.DATE_CREATED).descending()));
            List<TransactionFetchResponse> m = filter(UserType.PARTNER, userId.toString(), l);

            return m;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }


    public @NotNull List<TransactionRepository.TransactionSummary> weeklyTransactionSummary(UUID userId, LocalDateTime from, LocalDateTime to) {
        try {
            logger.info(" {} {} {}", userId, from, to);

            return transactionRepository.findWeeklyTransactionByUserId(userId, from, to);
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public @NotNull List<TransactionRepository.TransactionSummary> weeklyTransactionSummaryByPartnerId(UUID partnerId,String msisdn,String currency, LocalDateTime from, LocalDateTime to) {
        try {
            logger.info(" {} {} {} {} {} ", partnerId,msisdn,currency, from, to);

            return transactionRepository.findTransactionByPartnerId(partnerId,msisdn,currency, from, to);
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }


    public DashboardTransactionSummary mapRevenue(UUID userId, LocalDateTime from, LocalDateTime to, List<TransactionRepository.TransactionSummary>[] list) {

        Map<TransactionType, BigDecimal[]> mapTransactions = new HashMap<>();
        Map<TransactionType, BigDecimal[]> mapRevenue = new HashMap<>();

        Map<TransactionType, List<Object[]>> mapTrendTransactions = new HashMap<>();
        Map<TransactionType, List<Object[]>> mapTrendRevenue = new HashMap<>();

        for (int i = 0; i < list.length; i++) {
            int finalI = i;

            for (TransactionType type : TransactionType.values()) {// list[i].forEach(transactionSummary -> {
                List<TransactionRepository.TransactionSummary> transactionSummaryList = list[i].stream().filter(transactionSummary1 -> {
                    return transactionSummary1.getType().equals(type);
                }).collect(Collectors.toList());

                if (mapTransactions.get(type) == null) {
                    mapTransactions.put(type, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    mapTrendTransactions.put(type, new ArrayList());
                }
                if (mapRevenue.get(type) == null) {
                    mapRevenue.put(type, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    mapTrendRevenue.put(type, new ArrayList());
                }
                TransactionRepository.TransactionSummary transactionSummary = transactionSummaryList.isEmpty() ?
                        null : transactionSummaryList.get(0);
                if (transactionSummary == null) {
                    logger.info("no transactionSummary for {} {} {} {}", type, userId, from, to);
                    continue;
                }
                BigDecimal[] transaction = mapTransactions.get(transactionSummary.getType());
                transaction[finalI] = transaction[finalI].add(transactionSummary.getRecords());
                Object[] val = new Object[]{transactionSummary.getCreated().toInstant().atZone(ZoneId.systemDefault()).getDayOfYear()
                        , transactionSummary.getRecords()};
                mapTrendTransactions.get(transactionSummary.getType()).add(val);
                mapTransactions.put(transactionSummary.getType(), transaction);


                BigDecimal[] revenue = mapRevenue.get(transactionSummary.getType());
                Object[] val2 = new Object[]{transactionSummary.getCreated().toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek().name()
                        , transactionSummary.getPartnerRevenue()};
                mapTrendRevenue.get(transactionSummary.getType()).add(val2);
                revenue[finalI] = revenue[finalI].add(transactionSummary.getPartnerRevenue());
                mapRevenue.put(transactionSummary.getType(), revenue);

            }
        }

        DashboardTransactionSummary ds = new DashboardTransactionSummary();
        DashboardTransactionSummary.DashboardTransactionSummaryChild dscTransaction = new DashboardTransactionSummary.DashboardTransactionSummaryChild();
        DashboardTransactionSummary.DashboardTransactionSummaryChild dscRevenue = new DashboardTransactionSummary.DashboardTransactionSummaryChild();
        ds.setTransaction(dscTransaction);
        ds.setRevenue(dscRevenue);

        dscTransaction.setPrevCount(BigDecimal.ZERO);
        dscTransaction.setCurrentCount(BigDecimal.ZERO);
        dscRevenue.setPrevCount(BigDecimal.ZERO);
        dscRevenue.setCurrentCount(BigDecimal.ZERO);

        //fill in missing dates with zeros
        final int days = (int) ChronoUnit.DAYS.between(from, to) + 1;

        List<Object[]> trendTransactionTotal = new ArrayList<>(days + 1);
        for (int x = 0; x <= days; x++) {
            trendTransactionTotal.add(x, new Object[]{"", "0"});
        }

        mapTransactions.keySet().forEach(transactionType -> {
            DashboardTransactionSummary.DashboardTransactionSummaryChild dsc = new DashboardTransactionSummary.DashboardTransactionSummaryChild();

            dsc.setPrevCount(mapTransactions.get(transactionType)[0] == null ? BigDecimal.ZERO : mapTransactions.get(transactionType)[0]);
            dsc.setCurrentCount(mapTransactions.get(transactionType)[1] == null ? BigDecimal.ZERO : mapTransactions.get(transactionType)[1]);

          dscTransaction.setPrevCount(dscTransaction.getPrevCount().add(dsc.getPrevCount()));
            dscTransaction.setCurrentCount(dscTransaction.getCurrentCount().add(dsc.getCurrentCount()));
            if (mapTransactions.get(transactionType)[1].compareTo(BigDecimal.ZERO) != 0) {
                dsc.setPercentageChange(mapTransactions.get(transactionType)[1].subtract(mapTransactions.get(transactionType)[0]).divide(mapTransactions.get(transactionType)[1], 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)));

            } else {
                dsc.setPercentageChange(mapTransactions.get(transactionType)[0].multiply(new BigDecimal(100)));
            }

            List<Object[]> trendTransaction = new ArrayList<>(days + 1);
            for (int x = 0; x <= days; x++) {
                LocalDateTime dayx = to.minusDays(x);

                List<Object[]> l = mapTrendTransactions.get(transactionType).stream().filter(strings -> {
                    return strings[0].toString().equals(String.valueOf(dayx.getDayOfYear()));
                }).collect(Collectors.toList());
                final int[] t = {new Integer(0)};
                l.forEach(strings -> {
                    t[0] = t[0] + Integer.valueOf(strings[1].toString());
                });
                t[0] = t[0] + Integer.valueOf(trendTransactionTotal.get(x)[1].toString());
                trendTransactionTotal.set(x, new Object[]{dayx.getDayOfWeek().name(), t[0]});
                if (l.isEmpty()) {
                    trendTransaction.add(new Object[]{dayx.getDayOfWeek().name(), 0});
                    //   dscTransaction.getAdditionalProperties().put("transaction", trendTransactionTotal);
                } else {
                    trendTransaction.add(new Object[]{dayx.getDayOfWeek().name(), (l.get(0)[1])});
                }

            }
            trendTransaction.add(0, new String[]{"Day", transactionType.name()});


            dsc.setTrend(trendTransaction);
            dscTransaction.getAdditionalProperties().put(transactionType.name(), dsc);
        });


        dscTransaction.setTrend(trendTransactionTotal);

        List<Object[]> trendRevenueTotal = new ArrayList<>();
        for (int x = 0; x <= days; x++) {
            trendRevenueTotal.add(x, new String[]{"", "0"});
        }

        mapRevenue.keySet().forEach(transactionType -> {
            DashboardTransactionSummary.DashboardTransactionSummaryChild dsc = new DashboardTransactionSummary.DashboardTransactionSummaryChild();

            dsc.setPrevCount(mapRevenue.get(transactionType)[0] == null ? BigDecimal.ZERO : mapRevenue.get(transactionType)[0]);
            dsc.setCurrentCount(mapRevenue.get(transactionType)[1] == null ? BigDecimal.ZERO : mapRevenue.get(transactionType)[1]);

          dscRevenue.setPrevCount(dscRevenue.getPrevCount().add(dsc.getPrevCount()));
            dscRevenue.setCurrentCount(dscRevenue.getCurrentCount().add(dsc.getCurrentCount()));
            if (mapRevenue.get(transactionType)[1].compareTo(BigDecimal.ZERO) != 0) {
                dsc.setPercentageChange(mapRevenue.get(transactionType)[1].subtract(mapRevenue.get(transactionType)[0]).divide(mapRevenue.get(transactionType)[1], 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)));

            } else {
                dsc.setPercentageChange(mapRevenue.get(transactionType)[0].multiply(new BigDecimal(100)));
            }

            List<Object[]> trendRevenue = new ArrayList<>();
            for (int x = 0; x <= days; x++) {
                LocalDateTime dayx = to.minusDays(x);


                List<Object[]> l = mapTrendRevenue.get(transactionType).stream().filter(strings -> {
                    return strings[0].toString().equals(String.valueOf(dayx.getDayOfYear()));
                }).collect(Collectors.toList());
                final int[] t = {new Integer(0)};
                l.forEach(strings -> {
                    t[0] = t[0] + Integer.valueOf(strings[1].toString());
                });
                t[0] = t[0] + Integer.valueOf(trendTransactionTotal.get(x)[1].toString());
                trendRevenueTotal.set(x, new Object[]{dayx.getDayOfWeek().name(), t[0]});
                if (l.isEmpty()) {
                    trendRevenue.add(new Object[]{dayx.getDayOfWeek().name(), 0});
                    //   dscTransaction.getAdditionalProperties().put("transaction", trendTransactionTotal);
                } else {
                    trendRevenue.add(new Object[]{dayx.getDayOfWeek().name(), (l.get(0)[1])});
                }
            }

            dsc.setTrend(trendRevenue);
            dscRevenue.getAdditionalProperties().put(transactionType.name(), dsc);
        });

        dscRevenue.setTrend(trendRevenueTotal);


        //do total percentage
        if (ds.getTransaction().getPrevCount().compareTo(BigDecimal.ZERO) != 0) {
            ds.getTransaction().setPercentageChange(ds.getTransaction().getPrevCount().subtract(ds.getTransaction().getCurrentCount()).divide(ds.getTransaction().getPrevCount(), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)));

        } else {
            ds.getTransaction().setPercentageChange(ds.getTransaction().getCurrentCount().multiply(new BigDecimal(100)));
        }
        if (ds.getRevenue().getPrevCount().compareTo(BigDecimal.ZERO) != 0) {
            ds.getRevenue().setPercentageChange(
                ds.getRevenue().getPrevCount().subtract(ds.getRevenue().getCurrentCount())
                    .divide(ds.getRevenue().getPrevCount(), 2, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)));

        } else {
            ds.getRevenue().setPercentageChange(
                ds.getRevenue().getCurrentCount().multiply(new BigDecimal(100)));
        }

        ds.getRevenue().getTrend().add(0, new String[]{"Day", "Revenue"});
        ds.getTransaction().getTrend().add(0, new String[]{"Day", "Transaction"});
        return ds;
    }

    public @NotNull List<TransactionInit> fetchTransactionInit(UUID agentId,
        MakerCheckerStatus status,Integer checkerStage, Integer offset, Integer limit, LocalDate from,
        LocalDate to) {
        try {
            logger
                .info("fetchTransactionInit {} {} {} {} {} {} {} {}", agentId, status, checkerStage,
                    from, to, offset, limit);

            LocalDateTime f =
                from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
            LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();

//            if(agentId==null){
//                return transactionInitRepository.findSystemTransactionInit( status,checkerStage, f, t,
//                    new OffsetBasedPageRequest(offset, limit,
//                        Sort.by(Transaction.DATE_CREATED).descending()));
//            }else{
            return transactionInitRepository
                .findTransactionInit(agentId, status, checkerStage, f, t,
                    new OffsetBasedPageRequest(offset, limit,
                        Sort.by(Transaction.DATE_CREATED).descending()));
//            }

        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<TransactionInit>();
        }
    }

    public List<TransactionFetchResponse> fetchTransactionDetailLike(String id, Integer offset, Integer limit,
        LocalDate from, LocalDate to) {
        try {
            logger.info("fetchTransactionLike {} {} {}", id, from, to);

           List<Transaction> list = fetchTransactionLike(id,offset,limit,from,to);

            return filter(UserType.ADMIN, null, list);

        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<TransactionFetchResponse>();
        }
    }

    public List<Transaction> fetchTransactionLike(String id, Integer offset, Integer limit,
        LocalDate from, LocalDate to) {
        try {
            logger.info("fetchTransactionLike {} {} {}", id, from, to);

            LocalDateTime f =
                from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
            LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();

            return transactionRepository.findByTransactionLike(id, f, t,
                new OffsetBasedPageRequest(offset, limit,
                    Sort.by(Transaction.DATE_CREATED).descending()));
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Transaction>();
        }
    }

    public @NotNull List<Reversal> fetchReversal(
        MakerCheckerStatus status,Integer checkerStage, Integer offset, Integer limit, LocalDate from,
        LocalDate to) {
        try {
            logger.info("fetchReversal checkerStage {} {} {}", checkerStage,from, to);

            LocalDateTime f =
                from == null ? LocalDateTime.now().minusYears(1) : from.atStartOfDay();
            LocalDateTime t = to == null ? LocalDateTime.now() : to.atStartOfDay();

            return reversalRepository.findReversal(status,checkerStage, f, t,
                new OffsetBasedPageRequest(offset, limit,
                    Sort.by(Transaction.DATE_CREATED).descending()));//BaseEntity.toDbField(
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Reversal>();
        }
    }

}
