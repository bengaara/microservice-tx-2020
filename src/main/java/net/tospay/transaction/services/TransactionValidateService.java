package net.tospay.transaction.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.tospay.transaction.entities.Location;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UtilityType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.ChargeInfo;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.LocationFetch;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionLimit;
import net.tospay.transaction.repositories.LocationRepository;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidateService extends BaseService {


    TransactionLimitService transactionLimitService;

    LocationLimitService locationLimitService;

    ForexService forexService;

    CrudService crudService;

    DashboardService dashboardService;


    LocationRepository locationRepository;
    @Value("${location_limits.enable}")
    Boolean locationLimitEnable;

    public TransactionValidateService(TransactionLimitService transactionLimitService,
        ForexService forexService, CrudService crudService, DashboardService dashboardService,LocationLimitService locationLimitService,LocationRepository locationRepository) {

        this.transactionLimitService = transactionLimitService;
        this.locationLimitService =locationLimitService;

        this.forexService = forexService;
        this.crudService = crudService;
        this.dashboardService = dashboardService;
        this.locationRepository = locationRepository;

    }

    public List<Error> checkValidityErrors(TransactionRequest request) {

        List<Error> errors = new ArrayList();

        //
        if (request.getType() == null) {
            logger.debug("transaction type missing");
            errors.add(new Error(ResponseCode.TRANSACTION_TYPE_MISSING.type,
                ResponseCode.TRANSACTION_TYPE_MISSING.name()));
            return errors;
        }
        if (request.getOrderInfo() == null || request.getOrderInfo().getAmount() == null
            || request.getOrderInfo().getAmount().getCurrency() == null) {
            logger.debug("order info missing");
            errors.add(new Error(ResponseCode.ORDER_INFO_MISSING.type,
                ResponseCode.ORDER_INFO_MISSING.name()));
            return errors;
        }

        //SYS_OP_ACC/SYSTEM_LOAD has no source
        if (OrderType.SYSTEM_LOAD.equals(request.getOrderInfo().getType())) {
            logger.debug(" OrderType.SYS_OP_ACC - no source. inject dummy");

            //hack to add charge block if missing
            if (request.getChargeInfo() == null) {
                ChargeInfo chargeInfo = new ChargeInfo();
                chargeInfo
                    .setSource(new Amount(BigDecimal.ZERO, request.getOrderInfo().getAmount()
                        .getCurrency()));//TODO: remove currency type
                chargeInfo.setDestination(new Amount(BigDecimal.ZERO, "KSH"));
                request.setChargeInfo(chargeInfo);
            }

            if (request.getSource() == null || request.getSource().isEmpty()) {
                request.setSource(Arrays.asList(new Store()));
                request.getSource().get(0).setAccount(
                    (Account) Utils.deepCopy(request.getDelivery().get(0).getAccount()));
            }
            if (request.getSource().get(0).getCharge() == null) {
                request.getSource().get(0).setCharge(
                    new Amount(BigDecimal.ZERO, request.getOrderInfo().getAmount().getCurrency()));
            }
            if (request.getSource().get(0).getOrder() == null) {
                request.getSource().get(0).setOrder(request.getOrderInfo().getAmount());
            }
            if (request.getSource().get(0).getTotal() == null) {
                request.getSource().get(0).setTotal(new Amount(
                    request.getSource().get(0).getCharge().getAmount()
                        .add(request.getSource().get(0).getOrder().getAmount()),
                    request.getOrderInfo().getAmount().getCurrency()));
            }
        }
        //delivery is orderinfo
        if (request.getDelivery().get(0).getCharge() == null) {
            request.getDelivery().get(0).setCharge(
                new Amount(BigDecimal.ZERO, request.getOrderInfo().getAmount().getCurrency()));
        }
        if (request.getDelivery().get(0).getOrder() == null) {
            request.getDelivery().get(0).setOrder(request.getOrderInfo().getAmount());
        }
        if (request.getDelivery().get(0).getTotal() == null) {
            request.getDelivery().get(0).setTotal(new Amount(
                request.getOrderInfo().getAmount().getAmount()
                    .subtract(request.getDelivery().get(0).getCharge().getAmount()),
                request.getOrderInfo().getAmount().getCurrency()));
        }
        if (request.getSource().get(0).getTotal() == null) {
            request.getSource().get(0).setTotal(new Amount(
                request.getSource().get(0).getOrder().getAmount()
                    .add(request.getSource().get(0).getCharge().getAmount()),
                request.getOrderInfo().getAmount().getCurrency()));
        }
//            errors.add(new Error(ResponseCode.TRANSACTION_TYPE_MISSING.type, ResponseCode.TRANSACTION_TYPE_MISSING.name()));
//            return errors;

        if (request.getSource() == null || request.getSource().isEmpty()
            || request.getSource().stream().filter(store -> {
            return store.getAccount() == null || store.getTotal() == null
                || store.getOrder() == null || store.getCharge() == null;
        }).findFirst().isPresent()) {
            logger.debug(
                "Request must have at least one source with all required fields: account {}, total {},order {},charge {} {}",
                request.getSource().get(0).getAccount() == null,
                request.getSource().get(0).getTotal() == null,
                request.getSource().get(0).getOrder() == null,
                request.getSource().get(0).getCharge() == null);
            errors.add(new Error(ResponseCode.SOURCE_OR_DESTINATION_MISSING.type,
                String.format(
                    "Request must have at least one source with all required fields: account %b, total %b,order %b,charge %b {}",
                    request.getSource().get(0).getAccount() == null,
                    request.getSource().get(0).getTotal() == null,
                    request.getSource().get(0).getOrder() == null,
                    request.getSource().get(0).getCharge() == null)));
            return errors;

        }
        if (request.getDelivery() == null || request.getDelivery().isEmpty()
            || request.getDelivery().stream().filter(store -> {
            return store.getAccount() == null || store.getTotal() == null
                || store.getOrder() == null || store.getCharge() == null;
        }).findFirst().isPresent()) {
            logger.debug(
                "Request must have at least one delivery with all required fields: account {}, total {},order {},charge {} {}",
                request.getDelivery().get(0).getAccount() == null,
                request.getDelivery().get(0).getTotal() == null,
                request.getDelivery().get(0).getOrder() == null,
                request.getDelivery().get(0).getCharge() == null);
            errors.add(new Error(ResponseCode.SOURCE_OR_DESTINATION_MISSING.type,
                String.format(
                    "Request must have at least one delivery with all required fields: account {}, total {},order {},charge {} {}")));
            return errors;

        }
        //source id must exist
        if (request.getSource().stream().filter(store -> {
            return store.getAccount().getId() == null || store.getAccount().getId().isEmpty();
        }).findFirst().isPresent()) {
            logger.debug("source id required {} ");
            errors.add(new Error(ResponseCode.SOURCE_ID_REQUIRED.type,
                ResponseCode.SOURCE_ID_REQUIRED.name()));
            return errors;
        }
        //destination id must exist
        if (request.getDelivery().stream().filter(store -> {
            return store.getAccount().getId() == null || store.getAccount().getId().isEmpty();
        }).findFirst().isPresent()) {
            logger.debug("delivery id required {} ");
            errors.add(new Error(ResponseCode.DESTINATION_ID_REQUIRED.type,
                ResponseCode.DESTINATION_ID_REQUIRED.name()));
            return errors;
        }

        //source should not equal delivery
        Optional optional1 = request.getSource().stream().filter(store -> {
            Optional optional = request.getDelivery().stream().filter(store1 -> {
                return store.getAccount().getType().equals(store1.getAccount().getType()) && store
                    .getAccount().getId().equalsIgnoreCase(store1.getAccount().getId());
            }).findFirst();
            return optional.isPresent();

        }).findFirst();
        if (optional1.isPresent() && !OrderType.SYSTEM_LOAD
            .equals(request.getOrderInfo().getType())) {
            logger.debug("source and destination match. Cannot send money to same account {} ");
            errors.add(new Error(ResponseCode.SOURCE_MATCHES_DESTINATION.type,
                ResponseCode.SOURCE_MATCHES_DESTINATION.name()));
            return errors;
        }
        //source should not be less than zero
        if (!request.getSource().stream().filter(store -> {
            return BigDecimal.ZERO.compareTo(store.getTotal().getAmount())
                == -1;//zero and less not allowed
        }).findFirst().isPresent()) {
            logger.debug("source amount cannot be less than or equal MINIMUM (zero)");
            errors.add(new Error(ResponseCode.SOURCE_AMOUNT_LESS_THAN_MINIMUM.type,
                ResponseCode.SOURCE_AMOUNT_LESS_THAN_MINIMUM.name()));
            return errors;
        }
        //destination should not be less than zero
        if (!request.getDelivery().stream().filter(store -> {
            return BigDecimal.ZERO.compareTo(store.getTotal().getAmount())
                == -1;//zero and less not allowed
        }).findFirst().isPresent()) {
            logger.debug("destination cannot be less than MINIMUM (zero)");
            errors.add(new Error(ResponseCode.DESTINATION_AMOUNT_LESS_THAN_MINIMUM.type,
                ResponseCode.DESTINATION_AMOUNT_LESS_THAN_MINIMUM.name()));
            return errors;
        }

        if (TransactionType.SETTLEMENT.equals(request.getType())) {
            //skip validation
            logger.debug("type settlement - skip validate {}", request);
            return errors;
        }
        if (TransactionType.PAYMENT.equals(request.getType())
            && request.getOrderInfo().getToken() == null) {
            //skip validation
            logger.debug("TODO:telkom has no paymentservice  PAYMENT missing payment token {}",
                request.getOrderInfo().getReference());
            //    errors.add(new Error(ResponseCode.PAYMENT_TOKEN_MISSING.type, ResponseCode.PAYMENT_TOKEN_MISSING.name()));
            //    return errors;
        }

        AtomicReference<BigDecimal> sumSourceAmount = new AtomicReference<>(BigDecimal.ZERO);
        String currency = request.getOrderInfo().getAmount().getCurrency();
        if (request.getSource() != null) {
            request.getSource().forEach((topupValue) -> {

                if (topupValue.getCharge() != null && topupValue.getTotal().getAmount().compareTo(
                    topupValue.getOrder().getAmount().add(topupValue.getCharge().getAmount()))
                    != 0) {
                    logger.debug("Amounts don't tally: source total {} source order {} charge : {}",
                        topupValue.getTotal().getAmount(),
                        topupValue.getOrder().getAmount(), topupValue.getCharge().getAmount());
                    errors.add(new Error(ResponseCode.AMOUNT_MISMATCH.type,
                        String.format(
                            "Amounts don't tally: source total %s does not equal order info  %s plus charge %s ",
                            topupValue.getTotal().getAmount(), topupValue.getOrder().getAmount(),
                            topupValue.getCharge().getAmount())));

                }

                sumSourceAmount.updateAndGet(v -> v = v.add(topupValue.getTotal().getAmount()));
                if (!currency.equalsIgnoreCase(topupValue.getTotal().getCurrency())) {
                    logger.debug("currency mismatch orderinfo: {} source {}",
                        request.getOrderInfo().getAmount().getCurrency(), sumSourceAmount.get());
                    errors.add(new Error(ResponseCode.CURRENCY_MISMATCH.type, String
                        .format("orderinfo currency  %s and source dont match %s",
                            request.getOrderInfo().getAmount().getCurrency(),
                            topupValue.getTotal().getCurrency())));
                }
            });
        }

        request.setSumSourceAmount(
            new Amount(sumSourceAmount.get(), request.getOrderInfo().getAmount().getCurrency()));

        AtomicReference<BigDecimal> deliveryAmount = new AtomicReference<>(BigDecimal.ZERO);
//total sum should equal orderinfo plus charge
        BigDecimal sourceCharge = request.getChargeInfo().getSource().getAmount();
        if (sumSourceAmount.get()
            .compareTo(request.getOrderInfo().getAmount().getAmount().add(sourceCharge)) != 0) {
            logger.debug("Amounts don't tally: source  {} order info {}", sumSourceAmount.get(),
                request.getOrderInfo().getAmount().getAmount());
            errors.add(new Error(ResponseCode.AMOUNT_MISMATCH.type,
                String.format(
                    "Amounts don't tally: source  %s does not equal order info  %s plus charge %s ",
                    sumSourceAmount.get(), request.getOrderInfo().getAmount().getAmount(),
                    sourceCharge)));
        }

        AtomicReference<BigDecimal> destSourceAmount = new AtomicReference<>(BigDecimal.ZERO);
        BigDecimal destCharge = request.getChargeInfo().getDestination().getAmount();
        if (request.getDelivery() != null) {
            request.getDelivery().forEach((topupValue) -> {
                destSourceAmount.updateAndGet(v -> v = v.add(topupValue.getTotal().getAmount()));
                if (!currency.equalsIgnoreCase(topupValue.getTotal().getCurrency())) {
                    logger.debug("currency mismatch orderinfo: {} source {}",
                        request.getOrderInfo().getAmount().getCurrency(), sumSourceAmount.get());
                    errors.add(new Error(ResponseCode.CURRENCY_MISMATCH.type,
                        String.format("orderinfo currency %s and destination dont match  %s",
                            request.getOrderInfo().getAmount().getCurrency(),
                            topupValue.getTotal().getCurrency())));
                }
            });
        }
        if (destSourceAmount.get()
            .compareTo(request.getOrderInfo().getAmount().getAmount().subtract(destCharge))
            != 0) {//dest more than source-destcharge
            logger.debug("Amounts don't tally: destination  %s order info  %s charge %s  total  %s",
                destSourceAmount.get(),
                request.getOrderInfo().getAmount().getAmount(), destCharge, destCharge,
                request.getOrderInfo().getAmount().getAmount().subtract(destCharge));
            errors.add(new Error(ResponseCode.AMOUNT_MISMATCH.type,
                String.format(
                    "Amounts don't tally: destination  %s does not equal order info  %s plus charge %s ",
                    destSourceAmount.get(),
                    request.getOrderInfo().getAmount().getAmount(), destCharge)));
        }
        // check unique reference
        UUID partnerId = request.getChargeInfo().getPartnerInfo() != null ? request.getChargeInfo()
            .getPartnerInfo().getAccount().getUserId() : null;
        Transaction t = crudService
            .fetchTransactionByReference(request.getOrderInfo().getReference(), partnerId);
        if (t != null) {
            logger.debug("REFERENCE_ID_IN_USE {}", request.getOrderInfo().getReference());
            errors.add(new Error(ResponseCode.REFERENCE_ID_IN_USE.type,
                ResponseCode.REFERENCE_ID_IN_USE.name()));
        }

        errors.addAll(checkTransactionLimits(request));

        if (TransactionType.WITHDRAWAL.equals(request.getType())) {//telkom
            errors.addAll(checkLocationLimits(request));
        }

        return errors;

    }

    Error compareCurrency(Amount amount, Amount limit, boolean isBelowLimit,
        ResponseCode responseCode) {
        logger.debug("compareCurrency {} {} ", amount, limit);
        //   List<Error> errors = new ArrayList<>();
        if (amount == null || limit == null) {
            return null;
        }

        if (!limit.getCurrency().equalsIgnoreCase(amount.getCurrency())) {
            ResponseObject<ForexObject> response = forexService
                .hitForex(amount, new Amount(null, limit.getCurrency()));
            if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())
                && response.getData() != null) {
                amount = response.getData().getDestination();
            } else {
                //fail coz forex is down?
                amount = null;
                logger
                    .debug("TRANSACTION_LIMIT_FOREX_UNREACHABLE {}", limit);
                return new Error(ResponseCode.FOREX_UNREACHABLE.type,
                    ResponseCode.FOREX_UNREACHABLE.name());
            }
        }
        if (amount != null) {

            if (isBelowLimit) {
                if (limit.getAmount().compareTo(amount.getAmount()) < 0) {
                    logger.debug("TRANSACTION_LOWER_LIMIT_REACHED  {} {} {} {}", amount,
                        limit.getAmount(), isBelowLimit, responseCode);
                    return new Error(responseCode.type, responseCode.name());
                }
            } else {
                if (limit.getAmount().compareTo(amount.getAmount()) > 0) {
                    logger.debug("TRANSACTION_UPPER_LIMIT_REACHED  {} {} {} {}", amount,
                        limit.getAmount(), isBelowLimit, responseCode);
                    return new Error(responseCode.type, responseCode.name());
                }
            }


        }

        return null;
    }

    List<Error> checkTransactionLimits(TransactionRequest request) {

        logger.debug("checkTransactionLimits ");
        //,Account account
        List<Error> errors = new ArrayList<>();
        // boolean isSource = request.getUserInfo().getUserId().equals(account.getUserId());

        logger.debug("if {} = OrderType.SYSTEM_LOAD - fake source - will skip sourcing validate",
            request.getOrderInfo().getType());
        if (!OrderType.SYSTEM_LOAD.equals(request.getOrderInfo().getType())) {
            request.getSource().stream().forEach(store -> {

                ResponseObject<TransactionLimit> responseObject1 = transactionLimitService
                    .hitTransactionLimitService(store.getAccount().getUserId().toString(),
                        store.getAccount().getUserType(), store.getAccount().getSubType());
                if (ResponseCode.SUCCESS.type.equalsIgnoreCase(responseObject1.getStatus())
                    && responseObject1.getData() != null) {

                    Amount amount1 = (Amount) Utils.deepCopy(request.getSumSourceAmount());

                    errors.add(
                        compareCurrency(amount1, responseObject1.getData().getTxnLowerLimit(),
                            false,
                            ResponseCode.TRANSACTION_SOURCE_LOWER_LIMIT_REACHED));
                    errors
                        .add(
                            compareCurrency(amount1, responseObject1.getData().getTxnUpperLimit(),
                                true,
                                ResponseCode.TRANSACTION_SOURCE_UPPER_LIMIT_REACHED));
                    LocalDateTime f1 = LocalDateTime.now().toLocalDate().atStartOfDay();
                    List<Transaction> list1 = dashboardService
                        .fetchTransactionByMsisdn(request.getUserInfo().getUserId().toString(), 0
                            , 10000, f1, LocalDateTime.now());

                    final BigDecimal[] total1 = {BigDecimal.ZERO};
                    list1.stream().filter(transaction -> {

                        return transaction.getTransactionStatus().equals(TransactionStatus.SUCCESS);
                        //   transaction.getTransactionStatus().equals(TransactionStatus.FAILED));
                    }).forEach(t -> {
                        t.getSources().stream().forEach(s -> {
                            if (t.getUserInfo().getUserId()
                                .equals(request.getUserInfo().getUserId())) {
                                total1[0] = total1[0].add(s.getPayload().getTotal().getAmount());

                            }
                        });
                        t.getDestinations().stream().forEach(d -> {
                            if (!t.getUserInfo().getUserId()
                                .equals(request.getUserInfo().getUserId())) {
                                total1[0] = total1[0].add(d.getPayload().getTotal().getAmount());
                            }
                        });

                    });
                    if (responseObject1.getData().getDailyLimit() != null
                        && responseObject1.getData().getDailyLimit().getAmount().intValue() != 0) {
                        errors.add(compareCurrency(
                            new Amount(total1[0].add(amount1.getAmount()),
                                request.getOrderInfo().getAmount().getCurrency()),
                            responseObject1.getData().getDailyLimit(), true,
                            ResponseCode.TRANSACTION_SOURCE_UPPER_LIMIT_REACHED));
                    }

                    if (responseObject1.getData().getMaxDailyTransactions() != null
                        && responseObject1.getData().getMaxDailyTransactions().intValue() != 0
                        && responseObject1.getData().getMaxDailyTransactions().intValue() <= list1
                        .size()) {
                        logger.debug("TRANSACTION_LIMIT_REACHED getMaxDailyTransactions {} {}",
                            responseObject1.getData().getMaxDailyTransactions(), list1.size());
                        errors
                            .add(new Error(ResponseCode.TRANSACTION_SOURCE_RATE_LIMIT_REACHED.type,
                                ResponseCode.TRANSACTION_SOURCE_RATE_LIMIT_REACHED.name()));
                    }

                    //utility
                    if (TransactionType.UTILITY.equals(request.getType())
                        && request.getOrderInfo().getUtility() != null
                        &&
                        request.getOrderInfo().getUtility().getAdditionalProperties().get("action")
                            != null) {
                        String action = (String) request.getOrderInfo().getUtility()
                            .getAdditionalProperties().get("action");
                        UtilityType type = UtilityType.valueOfType(action);
                        logger.debug("TRANSACTION_LIMIT_CHECK for UTILITY  {} {}", action, type);

                        if (type != null && (UtilityType.AIRTIME.equals(type) || UtilityType.BUNDLE
                            .equals(type))) {
                            errors.add(
                                compareCurrency(amount1,
                                    responseObject1.getData().getAirtimeLimit(),
                                    true,
                                    ResponseCode.TRANSACTION_UTILITY_LIMIT_REACHED));

                        }


                    }
                } else {
                    logger.debug("TransactionLimit check failed {} {} {} ",
                        ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED,
                        request.getUserInfo().getUserId(),
                        request.getUserInfo().getTypeId());
                    errors.add(new Error(ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED.type,
                        ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED.name()));
                }

            });
        }

        //check destinations
        request.getDelivery().stream().forEach(store -> {

            ResponseObject<TransactionLimit> responseObject = transactionLimitService
                .hitTransactionLimitService(store.getAccount().getUserId().toString(),
                    store.getAccount().getUserType(), store.getAccount().getSubType());
            if (ResponseCode.SUCCESS.type.equalsIgnoreCase(responseObject.getStatus())
                && responseObject.getData() != null) {

                Amount amount = (Amount) Utils.deepCopy(store.getTotal());

                errors.add(
                    compareCurrency(amount, responseObject.getData().getTxnLowerLimit(), false,
                        ResponseCode.TRANSACTION_DELIVERY_LOWER_LIMIT_REACHED));
                errors.add(
                    compareCurrency(amount, responseObject.getData().getTxnUpperLimit(), true,
                        ResponseCode.TRANSACTION_DELIVERY_UPPER_LIMIT_REACHED));
                LocalDateTime f = LocalDateTime.now().toLocalDate().atStartOfDay();
                List<Transaction> list = dashboardService
                    .fetchTransactionByMsisdn(store.getAccount().getUserId().toString(), 0
                        , 10000, f, LocalDateTime.now());

                BigDecimal[] total = {BigDecimal.ZERO};
                list.stream().filter(deliver -> {

                    return deliver.getTransactionStatus().equals(TransactionStatus.SUCCESS);
                    //   transaction.getTransactionStatus().equals(TransactionStatus.FAILED));
                }).forEach(t -> {
                    t.getSources().stream().forEach(s -> {
                        if (t.getUserInfo().getUserId()
                            .equals(store.getAccount().getUserId())) {
                            total[0] = total[0].add(s.getPayload().getTotal().getAmount());
                        }
                    });
                    t.getDestinations().stream().forEach(d -> {
                        if (!t.getUserInfo().getUserId()
                            .equals(store.getAccount().getUserId())) {
                            total[0] = total[0].add(d.getPayload().getTotal().getAmount());
                        }
                    });
                });
                if (responseObject.getData().getDailyLimit() != null
                    && responseObject.getData().getDailyLimit().getAmount().intValue() != 0) {
                    errors.add(compareCurrency(
                        new Amount(total[0].add(amount.getAmount()),
                            request.getOrderInfo().getAmount().getCurrency()),
                        responseObject.getData().getDailyLimit(), true,
                        ResponseCode.TRANSACTION_DELIVERY_UPPER_LIMIT_REACHED));
                }
                if (responseObject.getData().getMaxDailyTransactions() != null
                    && responseObject.getData().getMaxDailyTransactions().intValue() != 0
                    && responseObject.getData().getMaxDailyTransactions().intValue() <= list
                    .size()) {
                    logger.debug("TRANSACTION_LIMIT_REACHED getMaxDailyTransactions {} {}",
                        responseObject.getData().getMaxDailyTransactions(), list.size());
                    errors.add(
                        new Error(ResponseCode.TRANSACTION_DELIVERY_RATE_LIMIT_REACHED.type,
                            ResponseCode.TRANSACTION_DELIVERY_RATE_LIMIT_REACHED.name()));
                }

                //if receiving cash
                if (responseObject.getData().getMaxBalance() != null
                    && responseObject.getData().getBalance() != null
                    && responseObject.getData().getMaxBalance().getAmount()
                    .compareTo(responseObject.getData().getBalance().getAmount()
                        .add(store.getTotal().getAmount()))
                    < 0) {
                    logger.debug("TRANSACTION_LIMIT_REACHED getBalance {} {} {}",
                        responseObject.getData().getBalance(),
                        responseObject.getData().getMaxBalance());
                    errors.add(new Error(ResponseCode.WALLET_DELIVERY_LIMIT_REACHED.type,
                        ResponseCode.WALLET_DELIVERY_LIMIT_REACHED.name()));
                }
            } else {
                logger.debug("TransactionLimit check failed {} {} {} ",
                    ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED, store.getAccount().getUserId(),
                    request.getUserInfo().getTypeId());
                errors.add(new Error(ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED.type,
                    ResponseCode.TRANSACTION_LIMIT_CHECK_FAILED.name()));
            }
        });

        errors.removeIf(Objects::isNull);
        return errors;
    }

    List<Error> checkLocationLimits(TransactionRequest request) {

        logger.debug("checkLocationLimits enabled_status: {} {}", locationLimitEnable);
        //,Account account
        List<Error> errors = new ArrayList<>();

        if (locationLimitEnable && request.getOrderInfo().getCellId() != null) {
            request.getDelivery().stream().forEach(store -> {

                ResponseObject<LocationFetch> responseObject1 = locationLimitService
                    .hitLocationLimitService(store.getAccount().getUserId());
                if (ResponseCode.SUCCESS.type.equalsIgnoreCase(responseObject1.getStatus())
                    && responseObject1.getData() != null) {

                    String latitude = responseObject1.getData().getLatitude();
                    String longitude = responseObject1.getData().getLongitude();

                    //TODO: get cell_id lat long of source from db
                    Optional<Location> opt = locationRepository
                        .findFirstByCellid(request.getOrderInfo().getCellId());
                   opt.ifPresent(location -> {

                   Double metres =    locationLimitService.meters(Double.valueOf(latitude),Double.valueOf(longitude),Double.valueOf(location.getLatitude()),Double.valueOf(location.getLongitude()));
                       logger.debug("Distance {} {} {}",metres,Constants.MAX_DISTANCE_LIMIT);
                      if(metres> Constants.MAX_DISTANCE_LIMIT){

                          errors.add(new Error(ResponseCode.WITHDRAWAL_DISTANCE_EXCEEDED.type,
                              ResponseCode.WITHDRAWAL_DISTANCE_EXCEEDED.name()));
                      }


                   });
                   // request.getOrderInfo().getCellId()

         //           errors.add(compareCurrency(amount1, responseObject1.getData().getTxnLowerLimit(), false, ResponseCode.TRANSACTION_SOURCE_LOWER_LIMIT_REACHED));
                }

            });
        }

        errors.removeIf(Objects::isNull);
        return errors;
    }

}
