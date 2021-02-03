package net.tospay.transaction.configs;

import java.util.concurrent.CompletableFuture;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.AsyncCallbackResponse;
import net.tospay.transaction.models.StoreStatusResponse;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.ReportingService;
import net.tospay.transaction.services.ReversalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

//scheduler should not be a bean @Component
@Configuration
public class JobSchedule {

    static boolean enableCheckAyncTransactionStatusCheck = true;
    private static int counter = 0;
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    FundService fundService;
    CrudService crudService;
    ReportingService reportingService;
    ReversalService reversalService;
    @Value("${cron.job.asyncTransactionStatusCheck.period}")
    int asyncTransactionStatusCheckPeriod;

    public JobSchedule(FundService fundService, CrudService crudService, ReportingService reportingService,ReversalService reversalService) {
        this.fundService = fundService;
        this.crudService = crudService;
        this.reportingService = reportingService;
        this.reversalService=reversalService;
    }

    public static boolean isEnableCheckAyncTransactionStatusCheck() {
        return enableCheckAyncTransactionStatusCheck;
    }

    public static void setEnableCheckAyncTransactionStatusCheck(boolean enable) {
        enableCheckAyncTransactionStatusCheck = enable;
        if (!enable) {
            counter = 0;
        }
    }

    @Scheduled(cron = "${cron.job.autoreversal}")
        // @Transactional
    void checkFailedTransactions() {
        try {
            logger.debug("Cron Task :: checkFailedTransactions  Execution Time - {} {}", LocalDateTime.now(),
                    Thread.currentThread().getName());

            LocalDateTime now = LocalDateTime.now(); // current date and time
            LocalDateTime midnight = now.toLocalDate().atStartOfDay();

            List<Transaction> list = crudService.fetchSourcedFailedUnreversedTransactions(midnight);

            list.forEach(transaction -> {
                Transaction reversalTransaction = reversalService.reverseTransaction(transaction, null);
                if(reversalTransaction !=null) {
                    //trigger sourcing async
                    CompletableFuture<Pair<Transaction, List<String>>> future = fundService
                        .pullFromSource(reversalTransaction);
                }
            });

        } catch (Exception e) {
            logger.error("", e);

        }
    }

    //NOTE run immediately after midnight
    @Scheduled(cron = "${cron.job.report}", zone = "Africa/Nairobi")
    public void prepareDaysReports() {
        try {
            logger.debug("Cron Task :: prepareTodayReports  Execution Time - {} {}", LocalDateTime.now(),
                    Thread.currentThread().getName());

            LocalDate midnight = LocalDateTime.now().toLocalDate();
            LocalDate yesterDay = midnight.minusDays(1);

            reportingService.prepareAllReports(yesterDay, midnight);
            TimeUnit.SECONDS.sleep(5);

        } catch (Exception e) {
            logger.error("", e);

        }
    }

    @Scheduled(cron = "${cron.job.asyncTransactionStatusCheck}")
        // @Transactional
    void checkAsyncTransactionStatusCheck() {
        try {
            if (!isEnableCheckAyncTransactionStatusCheck() || counter > 0){
                logger.debug("Cron Task :: checkAyncTransactionStatusCheck off {} {}",isEnableCheckAyncTransactionStatusCheck(),counter);
                return;
            }

            logger.debug("Cron Task :: checkAyncTransactionStatusCheck  Execution Time - {} {}", LocalDateTime.now(),
                    Thread.currentThread().getName());
            ++counter;

            LocalDateTime now = LocalDateTime.now(); // current date and time
            LocalDateTime from = now.minus(asyncTransactionStatusCheckPeriod, ChronoUnit.SECONDS);

            List<Transaction> list = crudService.fetchProcessingTransactions(from);
            List<Transaction> list1 = crudService.fetchPendingReversal(from);
            list.addAll(list1);

            logger.debug("checkAsyncTransactionStatusCheck ProcessingTransactions - count: {} {}", list.size());
            //  List<String[]> storeRefs = new ArrayList<>();
            list.forEach(transaction -> {
                logger.debug("Transactions still processing {} {}", transaction.getId(), transaction.getTransactionId());
                transaction.getSources().stream().filter(s -> {
                    logger.debug("Transactions source {} {} {}", transaction.getId(), s.getPayload().getAccount().getType(), s.getTransactionStatus());

                    if (TransactionStatus.FAILED.equals(s.getTransactionStatus())
                            || (AccountType.WALLET.equals(s.getPayload().getAccount().getType()) && TransactionStatus.PROCESSING.equals(s.getTransactionStatus()))) {
                        logger.debug("mark Transaction as failed {} {} {}", transaction.getId(), s.getPayload().getAccount().getType(), s.getTransactionStatus());
                        AsyncCallbackResponse res = new AsyncCallbackResponse();
                        res.setExternalReference(s.getId());
                        res.setCode(ResponseCode.FAILURE);
                        res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                        res.setDescription("TRANSACTION_ABORTED by the system");
                        fundService.processPaymentCallback(res);

                    }


                    return ((s.getTransactionStatus().equals(TransactionStatus.PROCESSING))
                            || (s.getTransactionStatus().equals(TransactionStatus.CREATED)));
                }).forEach(s -> {
                    this.logger.debug("{} {} {} StoreStatusCheckCount {} {}", s.getPayload().getAccount().getType(), s.getId(), s.getTransactionStatus(), s.getStoreStatusCheckCount(), s.getResponse());
                    //   if (s.getStoreRef() != null) {
                    //        String a[] = {s.getPayload().getAccount().getType().name(), s.getStoreRef()};
                    // ((Map)s.getResponse().values().toArray()[s.getResponse().size() -1]).get("store_ref").toString()};
                    s.setStoreStatusCheckCount(s.getStoreStatusCheckCount() + 1);
                    if (s.getStoreRef() == null) {
                        logger.debug("null storeref. failing the transaction {} {}", s.getId());
                        AsyncCallbackResponse res = new AsyncCallbackResponse();
                        res.setExternalReference(s.getId());
                        res.setCode(ResponseCode.FAILURE);
                        res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                        res.setDescription("TRANSACTION_ABORTED by the system");
                        fundService.processPaymentCallback(res);

                    }


                    ResponseObject<StoreStatusResponse> response = fundService.hitStoreStatus(s.getPayload().getAccount().getType(), s.getStoreRef());
                    if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                        AsyncCallbackResponse res = response.getData().toAsyncCallbackResponse();
                        if (res.getCode() != null && ResponseCode.PROCESSING.equals(res.getCode())
                                && s.getStoreStatusCheckCount() > 4) {//if still processing
                            logger.debug("TODO:StoreStatusCheckCount limit reached. marking as failed {} {}", s.getId());
                            res.setCode(ResponseCode.FAILURE);
                            res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                            res.setDescription("TRANSACTION_ABORTED by the system");
                        }

                        fundService.processPaymentCallback(res);


                    } else {
                        this.logger.debug("unknown tx status {} {} {} {} ", s.getId(), s.getStoreRef(), s.getTransactionStatus(), s.getResponse());

                    }
                    // storeRefs.add(a);
                    //}
                });

                if (!transaction.getSources().stream().filter(s -> {
                    return (!s.getTransactionStatus().equals(TransactionStatus.SUCCESS));
                }).findFirst().isPresent()) { //all sourced
                    logger.error("TODO:: hanging transaction - source complete,not delivered {} {}", transaction.getId());
                    // fundService.processTransactionStatus(transaction);
                    transaction.getDestinations().stream().filter(d -> {
                        logger.debug("Transactions  dest {} {} {}", transaction.getId(), d.getPayload().getAccount().getType(), d.getTransactionStatus());
                        if (TransactionStatus.FAILED.equals(d.getTransactionStatus())
                                || (AccountType.WALLET.equals(d.getPayload().getAccount().getType()) && TransactionStatus.PROCESSING.equals(d.getTransactionStatus()))) {
                            logger.debug("mark Transaction as failed {} {} {}", transaction.getId(), d.getPayload().getAccount().getType(), d.getTransactionStatus());
                            AsyncCallbackResponse res = new AsyncCallbackResponse();
                            res.setExternalReference(d.getId());
                            res.setCode(ResponseCode.FAILURE);
                            res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                            res.setDescription("TRANSACTION_ABORTED by the system");
                            fundService.processPaymentCallback(res);
                        }

                        return (
                                !AccountType.WALLET.equals(d.getPayload().getAccount().getType()) &&
                                        (d.getTransactionStatus().equals(TransactionStatus.PROCESSING))
                                        || (d.getTransactionStatus().equals(TransactionStatus.CREATED)));
                    }).forEach(d -> {
                        this.logger.debug(" {} {} StoreStatusCheckCount {} {}", d.getId(), d.getTransactionStatus(), d.getStoreStatusCheckCount(), d.getResponse());
                        //   if (s.getStoreRef() != null) {
                        //        String a[] = {s.getPayload().getAccount().getType().name(), s.getStoreRef()};
                        // ((Map)s.getResponse().values().toArray()[s.getResponse().size() -1]).get("store_ref").toString()};

                        d.setStoreStatusCheckCount(d.getStoreStatusCheckCount() + 1);
                        if (d.getStoreRef() == null) {
                            logger.debug("null storeref. failing the transaction {} {}", d.getId());
                            AsyncCallbackResponse res = new AsyncCallbackResponse();
                            res.setExternalReference(d.getId());
                            res.setCode(ResponseCode.FAILURE);
                            res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                            res.setDescription("TRANSACTION_ABORTED by the system");
                            fundService.processPaymentCallback(res);

                        }

                        ResponseObject<StoreStatusResponse> response = fundService.hitStoreStatus(d.getPayload().getAccount().getType(), d.getStoreRef());
                        if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) && response.getData() != null) {
                            AsyncCallbackResponse res = response.getData().toAsyncCallbackResponse();
                            if (res.getCode() != null && ResponseCode.PROCESSING.equals(res.getCode())
                                    && d.getStoreStatusCheckCount() > 2) {//if still processing
                                logger.debug("TODO:StoreStatusCheckCount limit reached. marking as failed {} {}", d.getId());
                                res.setCode(ResponseCode.FAILURE);
                                res.setReason(ResponseCode.TRANSACTION_ABORTED.name());
                                res.setDescription("TRANSACTION_ABORTED by the system");
                            }

                            fundService.processPaymentCallback(res);
                        } else {
                            this.logger.debug("TODO: unknown tx status {} {} {} {}", d.getId(), d.getStoreRef(), d.getTransactionStatus(), d.getResponse());
                        }
                    });
                }

            });
            --counter;
            JobSchedule.setEnableCheckAyncTransactionStatusCheck(false);


        } catch (Exception e) {
            logger.error("", e);

        }
    }


}
