package net.tospay.transaction.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.request.PaymentRequest;
import net.tospay.transaction.models.request.PaymentSplitResponse;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.MerchantInfo;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class FundService extends BaseService
{
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    @Autowired
    NotifyService notifyService;

    @Value("${numbergenerator.transaction.url}")
    String numberGeneratorTransactionUrl;

    @Value("${mobilepay.url}")
    private String mobilepayUrl;

    @Value("${walletpay.url}")
    private String walletpayUrl;

    @Value("${cardpay.url}")
    private String cardpayUrl;

    @Value("${splitpay.url}")
    private String splitPaymentUrl;
    @Value("${invoicepay.url}")
    private String invoicePaymentUrl;
    @Value("${paymentpay.url}")
    private String paymentPaymentUrl;

    public FundService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository, NotifyService notifyService)
    {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> sourcePay(net.tospay.transaction.entities.Transaction transaction)
    {
        try {

// Hibernate.initialize(transactionRepository);
//transactionRepository.refresh(transaction);

            transaction.setTransactionStatus(TransactionStatus.PROCESSING);
         //   List<Source> sources = transactionRepository.findById(transaction.getId()).get().getSources();
            List<Source> sources =transaction.getSources();
                    sources.forEach(source -> {

                source.setTransactionStatus(TransactionStatus.PROCESSING);
                TransferOutgoingRequest request = new TransferOutgoingRequest();
                request.setAccount(source.getAccount());
                request.setAction(Transaction.TransferAction.SOURCE);
                request.setAmount(source.getAmount());
                request.setUserId(source.getUserId());
                request.setUserType(source.getUserType());
                request.setCharge(source.getCharge());
                request.setCurrency(source.getCurrency());
                request.setDescription(source.getType().toString());
                request.setExternalReference(source.getId().toString());

                ResponseObject<StoreResponse> response = null;
                switch (source.getType()) {
                    case MOBILE: //async
                        response = hitMobile(request);
                        break;
                    case WALLET:
                        //process wallet response since its sync
                        response = hitWallet(request);
                        source.setDateResponse(new Timestamp(System.currentTimeMillis()));
                        break;
                    case CARD:
                        //process card response since its sync

                        TransactionRequest r = source.getTransaction().getPayload();
                        //   r.setAdditionalProperty("orderInfo ",request);

                        response = hitCard(r, request);
                        source.setDateResponse(new Timestamp(System.currentTimeMillis()));
                        break;
                    case BANK:
                        //TODO
                        source.setTransactionStatus(TransactionStatus.FAILED);
                        logger.debug("sourcing failed for bank TODO implementation {}", source);
                        break;
                }

                if (response != null) {
                    HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                    source.setResponse(node);
                }
                if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                    source.setTransactionStatus(TransactionStatus.FAILED);
                    logger.debug("sourcing failed  {}", source);
                } else {

                    TransactionStatus status =
                            ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                    TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
                    source.setTransactionStatus(status);
                    logger.debug("sourcing ok  {} {}", source, status);

                    //one success.. generate TR id
                    if (source.getTransaction().getTransactionId() == null) {

                        MerchantInfo merchantInfo = source.getTransaction().getPayload().getMerchantInfo();
                        ResponseObject<String> tr = generateTransactionId(merchantInfo.getTypeId(),
                                source.getTransaction().getTransactionType(), merchantInfo.getCountryCode());
                        if (tr != null && ResponseCode.SUCCESS.type.equals(tr.getStatus())) {
                            logger.debug("transaction id {}",tr.getData());
                            source.getTransaction().setTransactionId(tr.getData());
                        }
                    }
                }

                source = sourceRepository.save(source);
                        transactionRepository.saveAndFlush(transaction);

            });

            //transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
            checkSourceAndDestinationTransactionStatusAndAct(sources.get(0).getTransaction());

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }
public void moveFundsToWallet(net.tospay.transaction.entities.Transaction transaction){


if(!TransactionStatus.FAILED.equals(transaction.getTransactionStatus())){
    logger.debug("cant refund transaction {} with status {}",transaction.getId(),transaction.getTransactionStatus());
    return;
}

    Double amount = 0.0;
    for (Source source : transaction.getSources()) {
        if(TransactionStatus.SUCCESS.equals(source.getTransactionStatus())) {
            amount += source.getAmount();
            logger.debug("adding sourced amount {} ",source.getAmount());
        }
    }
    for (Destination destination : transaction.getDestinations()) {
        if(TransactionStatus.SUCCESS.equals(destination.getTransactionStatus())) {
            amount -= destination.getAmount();
            logger.debug("deducting delivered amount {} ",destination.getAmount());
        }
    }
    if(amount<=0){
        logger.debug("cant refund transaction amount {} ",amount);
        return;
    }
    Source source = transaction.getSources().get(0);
    logger.debug("refunding transaction {} amount {} to user {}",transaction.getId(),amount,source.getUserId());

    source.setTransactionStatus(TransactionStatus.PROCESSING);
    TransferOutgoingRequest request = new TransferOutgoingRequest();
    request.setAccount(source.getAccount());
    request.setAction(Transaction.TransferAction.DESTINATION);
    request.setAmount(amount);
    request.setUserId(source.getUserId());
    request.setUserType(source.getUserType());
    request.setCharge(0.0);
    request.setCurrency(source.getCurrency());
    request.setDescription("REFUND");
    request.setExternalReference(transaction.getId().toString());

    Destination destination= new Destination();
    destination.setTransaction(transaction);
    destination.setAccount(source.getAccount());
    destination.setAmount(request.getAmount());
    destination.setCurrency(request.getCurrency());
    destination.setTransactionStatus(TransactionStatus.REVERSED);
    destination.setType(AccountType.WALLET);
    destination.setUserId(request.getUserId());
    destination.setUserType(request.getUserType());

    ResponseObject<StoreResponse> response = hitWallet(request);

    if (response != null) {
        HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
        destination.setResponse(node);
        destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
    }
    if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
        destination.setTransactionStatus(TransactionStatus.FAILED);
    } else {
        TransactionStatus status =
                ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                        TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
        destination.setTransactionStatus(status);
        transaction.setTransactionStatus(TransactionStatus.REVERSED);
        logger.debug("destination ok transaction status : {}  {} {}",transaction.getTransactionStatus(), destination, status);
    }
    destination.setTransaction(transaction);
    transaction.addDestination(destination);
    destination = destinationRepository.save(destination);
    transactionRepository.saveAndFlush(transaction);



}

    public void checkSourceAndDestinationTransactionStatusAndAct(net.tospay.transaction.entities.Transaction transaction)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        //if all success - mark transaction source complete
        if (transaction == null) {
            logger.debug("null transaction ");
            return;
        }

        //only process incomplete transactions
        if (!Arrays.asList(TransactionStatus.PROCESSING, TransactionStatus.CREATED).contains(transaction.getTransactionStatus()))
        {
            logger.debug("cant process completed transaction  {} {}", transaction.getId(),transaction.getTransactionStatus());
            return;
        }

        AtomicBoolean sourcedSuccessAll = new AtomicBoolean(true);
        AtomicBoolean sourcedFail = new AtomicBoolean(false);
        transaction.getSources().forEach(s ->
        {
            sourcedSuccessAll
                    .set(sourcedSuccessAll.get() && TransactionStatus.SUCCESS
                            .equals(s.getTransactionStatus()));
            if (TransactionStatus.FAILED.equals(s.getTransactionStatus())) {
                sourcedFail.set(true);

                logger.debug("failed source {}", s.getId());
            }
        });
        logger.debug("mark transaction sourceComplete  {}  {}", transaction, sourcedSuccessAll);
        transaction.setSourceComplete(sourcedSuccessAll.get());

        if (sourcedFail.get()) {//if one failed

            logger.debug("mark transaction status  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.saveAndFlush(transaction);

            //notify
            notifyService.notifyTransferSource(transaction.getSources());
            //auto rollback funds to wallet?
            moveFundsToWallet(transaction);


            return;
        } else if (transaction.isSourceComplete() && !transaction.isDestinationStarted()) {
            //fire destination
            logger.debug("source complete - firing payDestination  {}", transaction);
            transaction.setDestinationStarted(true);
            transactionRepository.saveAndFlush(transaction);

            //TODO:actions
            logger.debug("TODO {}", transaction);
            //notify
            notifyService.notifyTransferSource(transaction.getSources());

            CompletableFuture<Boolean> future = payDestination(transaction);

            return;
        } else {
            logger.debug("transaction status :{}  {}", transaction.getId(),transaction.getTransactionStatus());
        }

        AtomicBoolean destinationSuccessAll = new AtomicBoolean(true);
        AtomicBoolean destinationFail = new AtomicBoolean(false);
        transaction.getDestinations().forEach(d ->
        {
            destinationSuccessAll
                    .set(destinationSuccessAll.get() && TransactionStatus.SUCCESS
                            .equals(d.getTransactionStatus()));

            if (TransactionStatus.FAILED.equals(d.getTransactionStatus())) {
                destinationFail.set(true);

                logger.debug("failed destination {}", d.getId());
            }
        });
        logger.debug("mark transaction destination Complete  {}  {}", transaction.getId(), destinationSuccessAll);

        transaction.setDestinationComplete(destinationSuccessAll.get());

        if (destinationFail.get()) {//if one failed

            logger.debug("mark transaction status failed  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.saveAndFlush(transaction);

            notifyService.notifyTransferDestination(transaction.getDestinations());

            //auto rollback funds to wallet?
            moveFundsToWallet(transaction);

            return;
        } else if (destinationSuccessAll.get() && !destinationFail.get()) {//successful
            logger.debug("mark transaction status success  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionRepository.saveAndFlush(transaction);

            //TODO:what actions?
            logger.debug("TODO  {}", transaction);
            notifyService.notifyTransferDestination(transaction.getDestinations());
            return;
        } else {
            logger.debug("TODO transaction status  {} {}", transaction.getId(),transaction.getTransactionStatus());
        }

        // if all paydestination : fire notification
        if (transaction.isDestinationComplete()) {

            logger.debug("TODO: destination complete - firing notify  {}", transaction);
            notifyService.notifyTransferDestination(transaction.getDestinations());

            //if payment transaction notify paymentservice
            if (TransactionType.PAYMENT.equals(transaction.getTransactionType())) {
                hitPaymentService(transaction);
            }
        }
    }

    public ResponseObject<PaymentSplitResponse> hitSplitPaymentService(
            net.tospay.transaction.entities.Transaction transaction)
    {

        try {
            logger.debug("hitSplitPaymentService {}", transaction);

            PaymentRequest request = new PaymentRequest();
            request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchant(transaction.getMerchantId());
            request.setReference(transaction.getExternalReference());
            request.setTransactionId(transaction.getTransactionId());
            request.setStatus(transaction.getTransactionStatus());

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentRequest>(request, headers);

            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject(splitPaymentUrl, entity, ResponseObject.class);
            logger.debug(" {}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    public ResponseObject<PaymentSplitResponse> hitPaymentService(
            net.tospay.transaction.entities.Transaction transaction)
    {

        try {
            logger.debug("hitPaymentService {}", transaction);
            String url = null;
            OrderType orderType =transaction.getSubType();
            switch (orderType){
                case QR:
                    url = paymentPaymentUrl;

                case INVOICE:
                    url = invoicePaymentUrl;
                case SPLIT:
                default:
                    logger.error("unhandled transaction subtype for {} {}", transaction.getId(), orderType);
                    break;
            }
            PaymentRequest request = new PaymentRequest();
            request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchant(transaction.getMerchantId());
            request.setReference(transaction.getExternalReference());
            request.setTransactionId(transaction.getTransactionId());
            request.setSenderId(transaction.getPayload().getUserInfo().getPhone());
            request.setStatus(transaction.getTransactionStatus());

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentRequest>(request, headers);

            logger.debug(" {}", request);
            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject(url, entity, ResponseObject.class);
            logger.debug(" {}", response);

            return response;


        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    public ResponseObject<StoreResponse> hitMobile(TransferOutgoingRequest request)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            logger.debug(" {}", objectMapper.writeValueAsString(request));
            ResponseObject<StoreResponse> response =
                    restTemplate.postForObject(mobilepayUrl, entity, ResponseObject.class);
            logger.debug(" {}", objectMapper.writeValueAsString(response));

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    public ResponseObject<StoreResponse> hitWallet(TransferOutgoingRequest request)
    {
        try {

            logger.debug(" {}", request);
            request.setAccount(null);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            logger.debug(" {}", objectMapper.writeValueAsString(request));
            ResponseObject<StoreResponse> response =
                    restTemplate.postForObject(walletpayUrl, entity, ResponseObject.class);
            logger.debug(" {}", objectMapper.writeValueAsString(response));

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    public ResponseObject<StoreResponse> hitCard(TransactionRequest request,
            TransferOutgoingRequest transferOutgoingRequest)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ObjectMapper objectMapper = new ObjectMapper();

            Map s = new ObjectMapper().convertValue(request, Map.class);
            Map m = new ObjectMapper().convertValue(transferOutgoingRequest, Map.class);
            m.put("reference", m.get("external_reference"));
            s.put("orderInfo", m);
            s.put("account", m.get("account"));

            logger.debug("{}", new ObjectMapper().writeValueAsString(s));
            HttpEntity entity = new HttpEntity<Map>(s, headers);

            ResponseObject<StoreResponse> response =
                    restTemplate.postForObject(cardpayUrl, entity, ResponseObject.class);
            logger.debug(" {}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> payDestination(final net.tospay.transaction.entities.Transaction transaction)
    {
        try {

            //if split bill enquire if sourcing complete
            if (TransactionType.PAYMENT.equals(transaction.getTransactionType())
            && OrderType.SPLIT.equals(transaction.getSubType()) ){
               ResponseObject<PaymentSplitResponse> res = hitSplitPaymentService(transaction);

                //if split complete pay merchant wallet
                if (res != null && ResponseCode.SUCCESS.type.equalsIgnoreCase(res.getStatus())) {
                    PaymentSplitResponse response = res.getData();
                    TransferOutgoingRequest request = new TransferOutgoingRequest();


                    //NOTE: MERCHANTINFO should be TOSPAY account
                    Account account = new Account();
                    account.setUserId(String.valueOf(response.getMerchantInfo().getUserId()));
                    account.setUserType(String.valueOf(response.getMerchantInfo().getTypeId()));
//                    account.setPhone(response.getMerchantInfo().getPhone());
//                    account.setName(response.getMerchantInfo().getName());
//                    account.setEmail(response.getMerchantInfo().getEmail());
                    request.setAccount(account);
                    request.setAction(Transaction.TransferAction.DESTINATION);
                    request.setUserId(response.getMerchantInfo().getUserId());
                    request.setUserType(response.getMerchantInfo().getTypeId());
                    request.setCurrency(response.getOrderInfo().getCurrency());
                    request.setDescription(response.getOrderInfo().getDescription());
                    request.setExternalReference(response.getOrderInfo().getReference());

                    Destination destination= new Destination();
                    destination.setTransaction(transaction);
                    destination.setAccount(account);

                    destination.setTransactionStatus(transaction.getTransactionStatus());
                    destination.setType(AccountType.WALLET);
                    destination.setUserId(response.getMerchantInfo().getUserId());
                    destination.setUserType(response.getMerchantInfo().getTypeId());


                    //split bill is many to one

                    ChargeDetail chargeDetail = transaction.getPayload().getChargeDetail();

                    request.setAmount(chargeDetail.getRecipientInfo().getAmount().getAmount());
                    request.setCurrency(chargeDetail.getRecipientInfo().getAmount().getCurrency());
                    destination.setAmount(request.getAmount());
                    destination.setCurrency(request.getCurrency());

                    ResponseObject<StoreResponse> r  = hitWallet(request);
                    if (r != null) {
                        HashMap<String, Object> node = mapper.convertValue(r, HashMap.class);
                        destination.setResponse(node);
                        destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
                    }
                    if (r == null || ResponseCode.FAILURE.type.equalsIgnoreCase(r.getStatus())) {//failure
                        destination.setTransactionStatus(TransactionStatus.FAILED);
                    } else {
                        TransactionStatus status =
                                ResponseCode.SUCCESS.type.equalsIgnoreCase(r.getStatus()) ?
                                        TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
                        destination.setTransactionStatus(status);
                        logger.debug("destination ok  {} {}", destination, status);
                    }

                    transaction.addDestination(destination);
                    destination = destinationRepository.save(destination);
                    transactionRepository.saveAndFlush(transaction);
                }else{
                    logger.warn("problem hitting payment service");
                    //problem with feedback from paymentservice
                    transaction.setTransactionStatus(TransactionStatus.PARTIAL_COMPLETE);
                    transactionRepository.saveAndFlush(transaction);
                }
                // transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
                checkSourceAndDestinationTransactionStatusAndAct(transaction);

                return CompletableFuture.completedFuture(true);
            } else {

                // Hibernate.initialize(transactionRepository);
                // transactionRepository.refresh(transaction);
                List<Destination> destinations = transaction
                        .getDestinations();//transactionRepository.findById(transaction.getId()).get().getDestinations();
                destinations.forEach(destination -> {

                    destination.setTransactionStatus(TransactionStatus.PROCESSING);
                    TransferOutgoingRequest request = new TransferOutgoingRequest();
                    request.setAccount(destination.getAccount());
                    request.setAction(Transaction.TransferAction.DESTINATION);
                    request.setAmount(destination.getAmount());
                    request.setUserId(destination.getUserId());
                    request.setUserType(destination.getUserType());
                    request.setDescription(destination.getType().toString());
                    request.setExternalReference(transaction.getExternalReference());

                    ChargeDetail chargeDetail = transaction.getPayload().getChargeDetail();

                    request.setAmount(chargeDetail.getRecipientInfo().getAmount().getAmount());
                    request.setCurrency(chargeDetail.getRecipientInfo().getAmount().getCurrency());
                    destination.setAmount(request.getAmount());
                    destination.setCurrency(request.getCurrency());


                    ResponseObject<StoreResponse> response = null;
                    switch (destination.getType()) {
                        case MOBILE:
                            response = hitMobile(request);
                            break;
                        case WALLET:
                            response = hitWallet(request);
                            destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
                            break;
                        case CARD:
//                                response = hitCard(request);
//                                destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
//                                break;
                            //TODO
                            destination.setTransactionStatus(TransactionStatus.FAILED);
                            logger.debug("destination failed for card TODO implementation {}", destination);
                            break;
                        case BANK:
                            //TODO
                            destination.setTransactionStatus(TransactionStatus.FAILED);
                            logger.debug("destination failed for bank TODO implementation {}", destination);
                            break;
                    }

                    if (response != null) {
                        HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                        destination.setResponse(node);
                    }

                    if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                        destination.setTransactionStatus(TransactionStatus.FAILED);
                    } else {
                        TransactionStatus status =
                                ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                        TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
                        destination.setTransactionStatus(status);
                        logger.debug("destination ok  {} {}", destination, status);
                    }

                    destination = destinationRepository.save(destination);
                    transactionRepository.save(transaction);
                });
                // transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
                checkSourceAndDestinationTransactionStatusAndAct(destinations.get(0).getTransaction());
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }


    public ResponseObject<String> generateTransactionId(UserType userType,
            TransactionType transactionType,
            String countryCode)
    {
        try {
            logger.debug("generateTransactionId");
            TransactionIdRequest request = new TransactionIdRequest();
            request.setUserType(userType);
            request.setCountry(countryCode);
            request.setTransactionType(transactionType);

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransactionIdRequest>(request, headers);

            ResponseObject<String> response =
                    restTemplate.postForObject(numberGeneratorTransactionUrl, entity, ResponseObject.class);
            logger.debug(" {}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);// e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error(" {}", e);
            return null;
        }
    }
    public void processPaymentCallback(UUID uuid,String status,Map node)

    {
        logger.debug(" {}", node);

        Optional<Source> optionalSource =uuid == null ? Optional.empty() :
                        sourceRepository.findById(uuid);
        Optional<net.tospay.transaction.entities.Destination> optionalDestination =
                uuid == null ? Optional.empty() :
                        destinationRepository.findById(uuid);

        net.tospay.transaction.entities.Transaction transaction = optionalSource.isPresent() ? optionalSource.get().getTransaction() :
                (optionalDestination.isPresent() ? optionalDestination.get().getTransaction() : null);

        TransactionStatus transactionStatus =
                ResponseCode.SUCCESS.type.equalsIgnoreCase(status) ? TransactionStatus.SUCCESS :
                        TransactionStatus.FAILED;
        if (optionalSource.isPresent()) {
            net.tospay.transaction.entities.Source source = optionalSource.get();
            source.setResponseAsync(node);
            source.setDateResponse(new Timestamp(System.currentTimeMillis()));
            source.setTransactionStatus(transactionStatus);
            source = sourceRepository.save(source);
            transaction = source.getTransaction();
            if (TransactionStatus.FAILED.equals(transactionStatus)) {
                logger.debug("sourcing failed  {}", source);
            }
        } else if (optionalDestination.isPresent()) {
            net.tospay.transaction.entities.Destination destination = optionalDestination.get();
            destination.setResponseAsync(node);
            destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
            destination.setTransactionStatus(transactionStatus);
            destination = destinationRepository.save(destination);
            transaction = destination.getTransaction();
            if (TransactionStatus.FAILED.equals(transactionStatus)) {
                logger.debug("paydestination failed  {}", destination);
            }
        } else {
            //TODO: callback from where?
            logger.error("callback called but no transaction found {}", node);
        }

        // transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);

        checkSourceAndDestinationTransactionStatusAndAct(transaction);;
    }
}
