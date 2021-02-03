package net.tospay.transaction.services;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import net.tospay.transaction.entities.BaseSource;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.FraudStatus;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.StoreActionType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.FraudCallback;
import net.tospay.transaction.models.FraudInfo;
import net.tospay.transaction.models.request.AuthHashOutgoingRequest;
import net.tospay.transaction.models.request.NotifyReferer;
import net.tospay.transaction.models.request.NotifyTransferData;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotifyService extends BaseService {

    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    @Value("${notify.transfer.url}")
    String notifyTransferUrl;

    @Value("${auth.hash.url}")
    String authHashUrl;


    @Value("${partnerinfo_email}")
    String partnerinfoEmail;

    DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");


    public NotifyService(RestTemplate restTemplate, TransactionRepository transactionRepository,
                         SourceRepository sourceRepository, DestinationRepository destinationRepository) {

        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    public void notifyDestination(Transaction transaction) {
        try {
            this.logger.debug("notifyDestination {}", transaction.getId());

            //notify fraud
            if (!transaction.isNotifiedFraud() && transaction.getPayload().getFraudInfo() != null) {

                transaction.setNotifiedFraud(true);
                hitFraud(transaction, transaction.getPayload().getFraudInfo());
            } else {
                this.logger.debug("already notified fraud . skip hitFraud {} {}", transaction.getId(), transaction.getTransactionStatus());
            }

            var ref = new Object() {
                boolean allowFailureCheck = false;
            };
            //TODO: find cooler fix
            if (partnerinfoEmail !=null && partnerinfoEmail.equalsIgnoreCase(transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getEmail())) {//if truecaller - notify failures too
                this.logger.debug("notify for failed delivery if truecaller.  {} {}",partnerinfoEmail, transaction.getId());
                ref.allowFailureCheck = true;
            }

            List<Destination> list = transaction.getDestinations();
            list.stream().filter(destination -> {

                return (destination.getTransactionStatus().equals(TransactionStatus.SUCCESS)
                     ||  (ref.allowFailureCheck && destination.getTransactionStatus().equals(TransactionStatus.FAILED)));
            })

//            .filter(destination -> {
//                if (Arrays.asList(TransactionType.TOPUP, TransactionType.WITHDRAW).contains(transaction.getType())) { //skip notify when type is withdrawal or topup since its the same source and destination - notification when after sourcing
//                    this.logger.debug("notifyDestination skipping transaction {} {}", transaction.getId(), transaction.getType());
//                    return false;
//                }
//                return true;
//
//            })
                    .filter(destination -> {
                        return !destination.isRevenue(); //don't notify revenue hits
                    }).filter(destination -> {
                    //only notify if transactionid was generated and success
                    if (transaction.getTransactionId() == null) {
                        this.logger.debug("skip notify. no transactionId for txid {} source {}", transaction.getId(), destination.getId());
                        return false;
                    }
                    return true;
                })


                    .forEach(d -> {
                        if (!d.isNotified()) {
                            this.logger.debug("notifyDestination {} {}", d.getId(), d.getTransactionStatus());
                            d.setNotified(true);
                            transactionRepository.save(d.getTransaction());
                            this.notifyGateway(d, StoreActionType.CREDIT);
                        } else {
                            this.logger.debug("already notified. skip notifyDestination {} {}", d.getId(), d.getTransactionStatus());
                        }
                    });


        } catch (Exception e) {
            this.logger.error("", e);
            return;
        }
    }


    public void notifySource(Transaction transaction) {
        try {
            this.logger.debug("notifySource {}", transaction.getId());

            //notify fraud
            if (!transaction.isNotifiedFraud() && transaction.getPayload().getFraudInfo() != null) {

                transaction.setNotifiedFraud(true);
                hitFraud(transaction, transaction.getPayload().getFraudInfo());
            } else {
                this.logger.debug("already notified fraud . skip hitFraud {} {}", transaction.getId(), transaction.getTransactionStatus());
            }

            //TODO: find cooler fix
            if (partnerinfoEmail !=null && partnerinfoEmail.equalsIgnoreCase(transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getEmail())
           ) {//if truecaller - skip source notify  && !transaction.getTransactionStatus().equals(TransactionStatus.FAILED)
                this.logger.debug("skip notify for source if truecaller.  {} {} {}",partnerinfoEmail, transaction.getId(),transaction.getTransactionStatus());
                return ;
            }

            List<Source> list = transaction.getSources();
            list.stream().filter(source -> {
                return (source.getTransactionStatus().equals(TransactionStatus.SUCCESS) ||
                        source.getTransactionStatus().equals(TransactionStatus.FAILED));
            })
                    .filter(source -> {
                //only notify if transactionid was generated and success
                if (transaction.getTransactionId() == null && source.getTransactionStatus().equals(TransactionStatus.SUCCESS)) {
                    this.logger.debug("skip notify. no transactionId for txid {} source {}", transaction.getId(), source.getId());
                    return false;
                }
                return true;
            }).filter(source -> {
                AtomicBoolean match = new AtomicBoolean(false);
                //skip if source n dest match
                transaction.getDestinations().stream().takeWhile(destination -> {
                    if (source.getTransactionStatus().equals(TransactionStatus.SUCCESS) && source.getPayload().getAccount().getUserId().equals(destination.getPayload().getAccount().getUserId())) {
                        this.logger.debug("skip notify. same user for source and dest - wait for dest leg {}", destination.getPayload().getAccount().getUserId());
                        return true;
                    }
                    return false;
                }).findFirst().ifPresent(destination -> {
                    match.set(true);
                });

                return !match.get();
            })
                    .forEach(d -> {

                                if (!d.isNotified()) {
                                    this.logger.debug("notifySource {} {}", d.getId(), d.getTransactionStatus());
                                    d.setNotified(true);
                                    transactionRepository.save(d.getTransaction());
                                    this.notifyGateway(d, StoreActionType.DEBIT);

                                } else {
                                    this.logger.debug("already notified. skip notifySource {} {}", d.getId(), d.getTransactionStatus());
                                }
                            }
                    );

        } catch (Exception e) {
            this.logger.error("", e);
            return;
        }
    }

    void notifyGateway(BaseSource entity, StoreActionType operation) {
        this.logger.debug("notifyGateway {} {}", entity.getId(), operation);

        NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();

        request.setNotificationType(Notify.Category
            .getCategory(entity.getTransaction().getType(), operation,
                entity.getTransaction().isCommission()));
        request.setStatus(entity.getTransactionStatus());
        request.setRecipientId(entity.getPayload().getAccount().getUserId().toString());
        request.setRecipientType(entity.getPayload().getAccount().getUserType());

        if (entity.getPayload().getAccount().getCallbackUrl() != null) {
            this.logger.debug("entity.getPayload().getAccount().getCallbackUrl() {} {}",
                entity.getPayload().getAccount().getCallbackUrl(), operation);
            request.setCallbackUrl(entity.getPayload().getAccount().getCallbackUrl());
        } else if (
            entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount()
                .getCallbackUrl() != null) {
            this.logger.debug(
                "entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl() {} {}",
                entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo()
                    .getAccount().getCallbackUrl(), operation);
            request.setCallbackUrl(
                entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount()
                    .getCallbackUrl());
        }

        NotifyTransferData data = new NotifyTransferData();
        data.setTopic(entity.getTransaction().getType());
        data.setUtility(entity.getTransaction().getPayload().getOrderInfo().getUtility());
        data.setAction(StoreActionType.DEBIT.equals(operation) ? "sent" : "received");
        if (data.getUtility() != null) {

            String action = String
                .valueOf(data.getUtility().getAdditionalProperties()
                    .get("action")); //BUY_AIRTIME,BUY_BUNDLE,UNSUBSCRIBE_BUNDLE
            action = action.replace("_", " ").replace("BUY", "BOUGHT")
                .replace("UNSUBSCRIBE", "UNSUBSCRIBED").toLowerCase();

            data.setAction(action);
        }
        data.setPreposition("to");
        if (TransactionType.WITHDRAWAL.equals(entity.getTransaction().getType()) && StoreActionType.DEBIT.equals(operation)) {
            data.setAction("withdrawn");
            data.setPreposition("from");
        }

        data.setStatus(entity.getTransactionStatus());
        data.setCode(entity.getCode());
        data.setReason(entity.getReason());
        data.setDescription(entity.getTransaction().getPayload().getOrderInfo().getDescription());

        data.setTotal(entity.getPayload().getTotal().getAmount());
        data.setAmount(entity.getPayload().getOrder().getAmount());
        data.setCurrency(entity.getPayload().getTotal().getCurrency());
        data.setCharge(entity.getPayload().getCharge() != null ? entity.getPayload().getCharge().getAmount() : BigDecimal.ZERO);
        data.setNewBalance(entity.getAvailableBalance());

        if(TransactionStatus.SUCCESS.equals(entity.getTransaction().getTransactionStatus()) && entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo() !=null && entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAmount() !=null){
            data.setPartnerRevenue(entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAmount().getAmount());
        }

        data.setTransactionId(entity.getTransaction().getTransactionId());
        data.setId(entity.getTransaction().getId());
        if (entity.getTransaction().getPayload().getOrderInfo() != null) {
            data.setReference(entity.getTransaction().getPayload().getOrderInfo().getReference());
        } else {
            this.logger.debug("notifySource order missing for {} {}", entity.getId(), operation);
        }

        if (StoreActionType.DEBIT.equals(operation)) {
            data.setReferer(
                entity.getTransaction().getDestinations().get(0).getPayload().getAccount());
        } else {
            if (entity.getTransaction().getSources().size() > 0) {
                data.setReferer(
                    entity.getTransaction().getSources().get(0).getPayload().getAccount());
            } else {
                data.setReferer(
                    entity.getTransaction().getDestinations().get(0).getPayload().getAccount());
            }
        }

        data.getReferer().setAccountType(
            UserType.PERSONAL.equals(data.getReferer().getUserType()) ? "SUBSCRIBER"
                : data.getReferer().getUserType().name());//of referer

        data.setDate(Utils.FORMATTER_DAY_TIME.format(entity.getTransaction().getDateCreated()));
        data.setOperation(operation);

        data.setChannel(entity.getPayload().getAccount().getType().name());


        //        if(entity.getPayload().getAccount().getCountry() !=null){
//            final List<String> timeZones = Stream.of(TimeZone.getAvailableIDs())
//                    .filter(zoneId -> zoneId.startsWith(entity.getPayload().getAccount().getCountry().getIso())).collect(Collectors.toList());
//          String date =  Utils.FORMATTER.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timeZones.get(0))) .toLocalDateTime());
//            request.setDate(date);
//
//        }
        data.setDate(Utils.FORMATTER_DAY_TIME.format(entity.getTransaction().getDateCreated().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));

        List<NotifyReferer> list1 = new ArrayList<>();
        entity.getTransaction().getSources().forEach(ds -> {
            NotifyReferer sender = new NotifyReferer(
                    ds.getPayload().getAccount().getUserId(),
                    ds.getPayload().getAccount().getUserType().name());
            list1.add(sender);
        });
        data.setSenders(list1.size() > 0 ? list1 : null);
        List<NotifyReferer> list2 = new ArrayList<>();
        entity.getTransaction().getDestinations().forEach(ds -> {

            NotifyReferer receiver = new NotifyReferer(
                    ds.getPayload().getAccount().getUserId(),
                    ds.getPayload().getAccount().getUserType().name());
            list2.add(receiver);
        });
        data.setReceivers(list2.size() > 0 ? list2 : null);

//        AuthHashOutgoingRequest r = new AuthHashOutgoingRequest();
//        r.setTimestamp(entity.getDateCreated().toEpochSecond(ZoneOffset.UTC));
//        r.setPartnerId(entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount().getPartnerId());
//        r.setUserId(entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount().getUserId());//TODO: hack za kings :-(
//        r.setAmount(data.getAmount());
//        r.setCurrency(data.getCurrency());
        //   ResponseObject<AuthHashOutgoingRequest> req = hitAuthHash(r);

        //    data.setHash(req != null && req.getData() != null ? req.getData().getHash() : null);


        request.setData(data);

        HashMap response = this.hitNotify(request);
    }
    void notifyGateway(BaseSource entity, StoreActionType operation,String message) {
        this.logger.debug("notifyGateway {} {} {}", entity.getId(), operation,message);

        NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();

        request.setNotificationType(Notify.Category
            .getCategory(entity.getTransaction().getType(), operation,
                entity.getTransaction().isCommission()));
        request.setStatus(entity.getTransactionStatus());
        request.setRecipientId(entity.getPayload().getAccount().getUserId().toString());
        request.setRecipientType(entity.getPayload().getAccount().getUserType());

        if (entity.getPayload().getAccount().getCallbackUrl() != null) {
            this.logger.debug("entity.getPayload().getAccount().getCallbackUrl() {} {}",
                entity.getPayload().getAccount().getCallbackUrl(), operation);
            request.setCallbackUrl(entity.getPayload().getAccount().getCallbackUrl());
        } else if (
            entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount()
                .getCallbackUrl() != null) {
            this.logger.debug(
                "entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl() {} {}",
                entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo()
                    .getAccount().getCallbackUrl(), operation);
            request.setCallbackUrl(
                entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAccount()
                    .getCallbackUrl());
        }

        NotifyTransferData data = new NotifyTransferData();
        data.setTopic(entity.getTransaction().getType());
        data.setUtility(entity.getTransaction().getPayload().getOrderInfo().getUtility());
        data.setAction(StoreActionType.DEBIT.equals(operation) ? "sent" : "received");
        if (data.getUtility() != null) {

            String action = String
                .valueOf(data.getUtility().getAdditionalProperties()
                    .get("action")); //BUY_AIRTIME,BUY_BUNDLE,UNSUBSCRIBE_BUNDLE
            action = action.replace("_", " ").replace("BUY", "BOUGHT")
                .replace("UNSUBSCRIBE", "UNSUBSCRIBED").toLowerCase();

            data.setAction(action);
        }
        if (TransactionType.WITHDRAWAL.equals(entity.getTransaction().getType()) && StoreActionType.DEBIT.equals(operation)) {
            data.setAction("withdrawn");
        }

        data.setStatus(entity.getTransactionStatus());
        data.setCode(entity.getCode());
        data.setReason(entity.getReason());
        data.setDescription(message);

        data.setTotal(entity.getPayload().getTotal().getAmount());
        data.setAmount(entity.getPayload().getOrder().getAmount());
        data.setCurrency(entity.getPayload().getTotal().getCurrency());
        data.setCharge(entity.getPayload().getCharge() != null ? entity.getPayload().getCharge().getAmount() : BigDecimal.ZERO);
        if(TransactionStatus.SUCCESS.equals(entity.getTransaction().getTransactionStatus()) && entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo() !=null && entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAmount() !=null){
            data.setPartnerRevenue(entity.getTransaction().getPayload().getChargeInfo().getPartnerInfo().getAmount().getAmount());
        }

        data.setTransactionId(entity.getTransaction().getTransactionId());
        data.setId(entity.getTransaction().getId());
        if (entity.getTransaction().getPayload().getOrderInfo() != null) {
            data.setReference(entity.getTransaction().getPayload().getOrderInfo().getReference());
        } else {
            this.logger.debug("notifySource order missing for {} {}", entity.getId(), operation);
        }

        if (StoreActionType.DEBIT.equals(operation)) {
            data.setReferer(
                entity.getTransaction().getDestinations().get(0).getPayload().getAccount());
        } else {
            if (entity.getTransaction().getSources().size() > 0) {
                data.setReferer(
                    entity.getTransaction().getSources().get(0).getPayload().getAccount());
            } else {
                data.setReferer(
                    entity.getTransaction().getDestinations().get(0).getPayload().getAccount());
            }
        }

        data.getReferer().setAccountType(
            UserType.PERSONAL.equals(data.getReferer().getUserType()) ? "SUBSCRIBER"
                : data.getReferer().getUserType().name());//of referer

        data.setDate(Utils.FORMATTER_DAY_TIME.format(entity.getTransaction().getDateCreated()));
        data.setOperation(operation);

        data.setChannel(entity.getPayload().getAccount().getType().name());


        //        if(entity.getPayload().getAccount().getCountry() !=null){
//            final List<String> timeZones = Stream.of(TimeZone.getAvailableIDs())
//                    .filter(zoneId -> zoneId.startsWith(entity.getPayload().getAccount().getCountry().getIso())).collect(Collectors.toList());
//          String date =  Utils.FORMATTER.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timeZones.get(0))) .toLocalDateTime());
//            request.setDate(date);
//
//        }
        data.setDate(Utils.FORMATTER_DAY_TIME.format(entity.getTransaction().getDateCreated().atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));

        List<NotifyReferer> list1 = new ArrayList<>();
        entity.getTransaction().getSources().forEach(ds -> {
            NotifyReferer sender = new NotifyReferer(
                ds.getPayload().getAccount().getUserId(),
                ds.getPayload().getAccount().getUserType().name());
            list1.add(sender);
        });
        data.setSenders(list1.size() > 0 ? list1 : null);
        List<NotifyReferer> list2 = new ArrayList<>();
        entity.getTransaction().getDestinations().forEach(ds -> {

            NotifyReferer receiver = new NotifyReferer(
                ds.getPayload().getAccount().getUserId(),
                ds.getPayload().getAccount().getUserType().name());
            list2.add(receiver);
        });
        data.setReceivers(list2.size() > 0 ? list2 : null);

        request.setData(data);

        HashMap response = this.hitNotify(request);
    }
    HashMap hitNotify(NotifyTransferOutgoingRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<NotifyTransferOutgoingRequest>(request, headers);

            this.logger.debug("sending notify {} {}", request, notifyTransferUrl);
            ResponseEntity<ResponseObject> response = this.restTemplate.postForEntity(this.notifyTransferUrl, entity, ResponseObject.class);
            this.logger.debug("{}", response);
            ResponseObject<HashMap> obj = response.getBody();
            return obj.getData();
        } catch (Exception e) {
            this.logger.error("", e);
            return null;
        }
    }

    void hitFraud(Transaction transaction, FraudInfo fraudInfo) {
        try {
            this.logger.debug("hitFraud  url: {}", fraudInfo.getCallBackUrl());
            FraudCallback request = new FraudCallback();
            request.setFraudQuery(fraudInfo.getFraudQuery());
            request.setTransactionStatus(transaction.getTransactionStatus());
            List<Source> list = transaction.getSources().stream().filter(source -> {
                return source.getReason() != null;
            }).collect(Collectors.toList());
            List<Destination> list2 = transaction.getDestinations().stream().filter(source -> {
                return source.getReason() != null;
            }).collect(Collectors.toList());
            if (list.size() > 0) {
                request.setCode(list.get(0).getCode());
                request.setReason(list.get(0).getReason());
            } else if (list2.size() > 0) {
                request.setCode(list2.get(0).getCode());
                request.setReason(list2.get(0).getReason());
            }


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<FraudCallback>(request, headers);
            this.logger.debug(" {}", request);
            ResponseEntity<ResponseObject> response =
                    this.restTemplate.postForEntity(fraudInfo.getCallBackUrl(), entity, ResponseObject.class);
            this.logger.debug(" {}", response);

            ResponseObject<BaseModel> obj = response.getBody();
            // return obj.getData();
        } catch (Exception e) {
            this.logger.error("", e);
            // return null;
        }
    }

    ResponseObject<AuthHashOutgoingRequest> hitAuthHash(AuthHashOutgoingRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<AuthHashOutgoingRequest>(request, headers);

            this.logger.debug("hitAuthHash {}", request);
            ResponseEntity<ResponseObject<AuthHashOutgoingRequest>> response = this.restTemplate.exchange(authHashUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ResponseObject<AuthHashOutgoingRequest>>() {
            });
            this.logger.debug("{}", response);
            if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getBody().getStatus())) {

                this.logger.debug("TODO: failure on getting settlement details {}", response);
            }
            return response.getBody();

        } catch (Exception e) {
            this.logger.error("", e);
            return null;
        }
    }
}
