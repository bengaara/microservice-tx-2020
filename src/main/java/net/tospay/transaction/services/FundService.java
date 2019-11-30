package net.tospay.transaction.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.MobilePayAction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.request.Account;
import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.request.PaymentSplitRequest;
import net.tospay.transaction.models.request.PaymentSplitResponse;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.request.TransferRequest;
import net.tospay.transaction.models.request.UserInfo;
import net.tospay.transaction.models.response.ChargeResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.MerchantInfo;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransferIncomingResponse;
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

    @Value("${charge.url}")
    String chargeUrl;

    @Value("${mobilepay.url}")
    private String mobilepayUrl;

    @Value("${walletpay.url}")
    private String walletpayUrl;

    @Value("${cardpay.url}")
    private String cardpayUrl;

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
    public CompletableFuture<Boolean> sourcePay(Transaction transaction)
    {
        try {

// Hibernate.initialize(transactionRepository);
//transactionRepository.refresh(transaction);

            transaction.setTransactionStatus(Transfer.TransactionStatus.PROCESSING);
         //   List<Source> sources = transactionRepository.findById(transaction.getId()).get().getSources();
            List<Source> sources =transaction.getSources();
                    sources.forEach(source -> {

                source.setTransactionStatus(Transfer.TransactionStatus.PROCESSING);
                TransferOutgoingRequest request = new TransferOutgoingRequest();
                request.setAccount(source.getAccount());
                request.setAction(MobilePayAction.SOURCE);
                request.setAmount(source.getAmount());
                request.setUserId(source.getUserId());
                request.setUserType(source.getUserType());
                request.setCharge(source.getCharge());
                request.setCurrency(source.getCurrency());
                request.setDescription(source.getType().toString());
                request.setExternalReference(source.getId().toString());

                ResponseObject<TransferIncomingResponse> response = null;
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

                        TransferRequest r = source.getTransaction().getPayload();
                        //   r.setAdditionalProperty("orderInfo ",request);

                        response = hitCard(r, request);
                        source.setDateResponse(new Timestamp(System.currentTimeMillis()));
                        break;
                    case BANK:
                        //TODO
                        source.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                        logger.debug("sourcing failed for bank TODO implementation {}", source);
                        break;
                }

                if (response != null) {
                    HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                    source.setResponse(node);
                }
                if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                    source.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                    logger.debug("sourcing failed  {}", source);
                } else {

                    Transfer.TransactionStatus status =
                            ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                    Transfer.TransactionStatus.SUCCESS : Transfer.TransactionStatus.PROCESSING;
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
                // transactionRepository.save(transaction);
                source = sourceRepository.save(source);
            });

            //transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
            checkSourceAndDestinationTransactionStatusAndAct(sources.get(0).getTransaction());

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }
public void moveFundsToWallet(Transaction transaction){


if(!Transfer.TransactionStatus.FAILED.equals(transaction.getTransactionStatus())){
    logger.debug("cant refund transaction {} with status {}",transaction.getId(),transaction.getTransactionStatus());
    return;
}

    Double amount = 0.0;
    for (Source source : transaction.getSources()) {
        if(Transfer.TransactionStatus.SUCCESS.equals(source.getTransactionStatus())) {
            amount += source.getAmount();
            logger.debug("adding sourced amount {} ",source.getAmount());
        }
    }
    for (Destination destination : transaction.getDestinations()) {
        if(Transfer.TransactionStatus.SUCCESS.equals(destination.getTransactionStatus())) {
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

    source.setTransactionStatus(Transfer.TransactionStatus.PROCESSING);
    TransferOutgoingRequest request = new TransferOutgoingRequest();
    request.setAccount(source.getAccount());
    request.setAction(MobilePayAction.DESTINATION);
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
    destination.setTransactionStatus(Transfer.TransactionStatus.REVERSED);
    destination.setType(Transfer.SourceType.WALLET);
    destination.setUserId(request.getUserId());
    destination.setUserType(request.getUserType());

    ResponseObject<TransferIncomingResponse> response = hitWallet(request);

    if (response != null) {
        HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
        destination.setResponse(node);
        destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
    }
    if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
        destination.setTransactionStatus(Transfer.TransactionStatus.FAILED);
    } else {
        Transfer.TransactionStatus status =
                ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                        Transfer.TransactionStatus.SUCCESS : Transfer.TransactionStatus.PROCESSING;
        destination.setTransactionStatus(status);
        transaction.setTransactionStatus(Transfer.TransactionStatus.REVERSED);
        logger.debug("destination ok transaction status : {}  {} {}",transaction.getTransactionStatus(), destination, status);
    }

    transaction.addDestination(destination);
    destination = destinationRepository.save(destination);



}

    public void checkSourceAndDestinationTransactionStatusAndAct(Transaction transaction)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        //if all success - mark transaction source complete
        if (transaction == null) {
            logger.debug("null transaction ");
            return;
        }

        //only process incomplete transactions
        if (Transfer.TransactionStatus.SUCCESS.equals(transaction.getTransactionStatus())
                || Transfer.TransactionStatus.FAILED.equals(transaction.getTransactionStatus())
                || Transfer.TransactionStatus.REVERSED.equals(transaction.getTransactionStatus()))
        {
            logger.debug("cant process completed transaction  {} {}", transaction.getId(),transaction.getTransactionStatus());
            return;
        }

        AtomicBoolean sourcedSuccessAll = new AtomicBoolean(true);
        AtomicBoolean sourcedFail = new AtomicBoolean(false);
        transaction.getSources().forEach(s ->
        {
            sourcedSuccessAll
                    .set(sourcedSuccessAll.get() && Transfer.TransactionStatus.SUCCESS
                            .equals(s.getTransactionStatus()));
            if (Transfer.TransactionStatus.FAILED.equals(s.getTransactionStatus())) {
                sourcedFail.set(true);

                logger.debug("failed source {}", s.getId());
            }
        });
        logger.debug("mark transaction sourceComplete  {}  {}", transaction, sourcedSuccessAll);
        transaction.setSourceComplete(sourcedSuccessAll.get());

        if (sourcedFail.get()) {//if one failed

            logger.debug("mark transaction status  {}", transaction);
            transaction.setTransactionStatus(Transfer.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            //notify
            notifyService.notifyTransferSource(transaction.getSources());
            //auto rollback funds to wallet?
            moveFundsToWallet(transaction);


            return;
        } else if (transaction.isSourceComplete() && !transaction.isDestinationStarted()) {
            //fire destination
            logger.debug("source complete - firing payDestination  {}", transaction);
            transaction.setDestinationStarted(true);
            transactionRepository.save(transaction);

            //TODO:actions
            logger.debug("TODO {}", transaction);
            //notify
            notifyService.notifyTransferSource(transaction.getSources());

            CompletableFuture<Boolean> future = payDestination(transaction);

            return;
        } else {
            logger.debug("TODO unknown transaction source state  {}", transaction.getId());
        }

        AtomicBoolean destinationSuccessAll = new AtomicBoolean(true);
        AtomicBoolean destinationFail = new AtomicBoolean(false);
        transaction.getDestinations().forEach(d ->
        {
            destinationSuccessAll
                    .set(destinationSuccessAll.get() && Transfer.TransactionStatus.SUCCESS
                            .equals(d.getTransactionStatus()));
        });
        logger.debug("mark transaction destination Complete  {}  {}", transaction.getId(), destinationSuccessAll);

        transaction.setDestinationComplete(destinationSuccessAll.get());

        if (destinationSuccessAll.get() && destinationFail.get()) {//if one failed

            logger.debug("mark transaction status failed  {}", transaction);
            transaction.setTransactionStatus(Transfer.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            notifyService.notifyTransferDestination(transaction.getDestinations());

            //auto rollback funds to wallet?
            moveFundsToWallet(transaction);

            return;
        } else if (destinationSuccessAll.get() && !destinationFail.get()) {//successful
            logger.debug("mark transaction status success  {}", transaction);
            transaction.setTransactionStatus(Transfer.TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            //TODO:what actions?
            logger.debug("TODO  {}", transaction);
            notifyService.notifyTransferDestination(transaction.getDestinations());
            return;
        } else {
            logger.debug("TODO unknown transaction destination state  {}", transaction);
        }

        // if all paydestination : fire notification
        if (transaction.isDestinationComplete()) {

            logger.debug("TODO: destination complete - firing notify  {}", transaction);
            notifyService.notifyTransferDestination(transaction.getDestinations());
        }
    }

    public ResponseObject<PaymentSplitResponse> hitSplitBillPaymentService(Source source)
    {

        try {

            logger.debug(" {}", source);

            PaymentSplitRequest request = new PaymentSplitRequest();
            request.setEmail(source.getTransaction().getPayload().getUserInfo().getEmail());
            request.setMerchant(source.getTransaction().getMerchantId());
            request.setReference(source.getTransaction().getExternalReference());
            request.setStatus(source.getTransactionStatus());

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentSplitRequest>(request, headers);

            logger.debug(" {}", source);
            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject(mobilepayUrl, entity, ResponseObject.class);
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

    public ResponseObject<TransferIncomingResponse> hitMobile(TransferOutgoingRequest request)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            logger.debug(" {}", objectMapper.writeValueAsString(request));
            ResponseObject<TransferIncomingResponse> response =
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

    public ResponseObject<TransferIncomingResponse> hitWallet(TransferOutgoingRequest request)
    {
        try {

            logger.debug(" {}", request);
            request.setAccount(null);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            logger.debug(" {}", objectMapper.writeValueAsString(request));
            ResponseObject<TransferIncomingResponse> response =
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

    public ResponseObject<TransferIncomingResponse> hitCard(TransferRequest request,
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

            ResponseObject<TransferIncomingResponse> response =
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
    public CompletableFuture<Boolean> payDestination(Transaction transaction)
    {
        try {

            //if split bill enquire if sourcing complete
            if (Transfer.TransactionType.SPLIT.equals(transaction.getTransactionType())) {

                AtomicReference<ResponseObject<PaymentSplitResponse>> res = null;

                boolean result = transaction.getSources().stream().anyMatch(source -> {
                    ResponseObject<PaymentSplitResponse> r =   hitSplitBillPaymentService(source);
                    logger.debug("{}", res);
                    if (res.get() != null && ResponseCode.FAILURE.type.equalsIgnoreCase(res.get().getStatus())) {
                        logger.debug("split bill sourcing complete");
                        res.set(r);
                        return true;
                    }
return false;
                });
                //if split complete pay merchant wallet
                if (res.get() != null && ResponseCode.FAILURE.type.equalsIgnoreCase(res.get().getStatus())) {
                    PaymentSplitResponse response = res.get().getData();
                    TransferOutgoingRequest request = new TransferOutgoingRequest();

                    Account account = new Account();
                    account.setUserId(String.valueOf(response.getMerchantInfo().getUserId()));
                    account.setUserType(String.valueOf(response.getMerchantInfo().getTypeId()));
//                    account.setPhone(response.getMerchantInfo().getPhone());
//                    account.setName(response.getMerchantInfo().getName());
//                    account.setEmail(response.getMerchantInfo().getEmail());
                    request.setAccount(account);
                    request.setAction(MobilePayAction.DESTINATION);
                    request.setAmount(response.getOrderInfo().getAmount());
                    request.setUserId(response.getMerchantInfo().getUserId());
                    request.setUserType(response.getMerchantInfo().getTypeId());
                    //TODO request.setCharge(destination.getCharge());
                    request.setCurrency(response.getOrderInfo().getCurrency());
                    request.setDescription(response.getOrderInfo().getDescription());
                    request.setExternalReference(response.getOrderInfo().getReference());


                    Destination destination= new Destination();
                    destination.setTransaction(transaction);
                    destination.setAccount(account);
                    destination.setAmount(request.getAmount());
                    destination.setCurrency(transaction.getCurrency());
                    destination.setTransactionStatus(transaction.getTransactionStatus());
                    destination.setType(Transfer.SourceType.WALLET);
                    destination.setUserId(response.getMerchantInfo().getUserId());
                    destination.setUserType(response.getMerchantInfo().getTypeId());

                    ResponseObject<TransferIncomingResponse> r  = hitWallet(request);
                    if (r != null) {
                        HashMap<String, Object> node = mapper.convertValue(r, HashMap.class);
                        destination.setResponse(node);
                        destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
                    }
                    if (r == null || ResponseCode.FAILURE.type.equalsIgnoreCase(r.getStatus())) {//failure
                        destination.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                    } else {
                        Transfer.TransactionStatus status =
                                ResponseCode.SUCCESS.type.equalsIgnoreCase(r.getStatus()) ?
                                        Transfer.TransactionStatus.SUCCESS : Transfer.TransactionStatus.PROCESSING;
                        destination.setTransactionStatus(status);
                        logger.debug("destination ok  {} {}", destination, status);
                    }

                    transaction.addDestination(destination);
                    destination = destinationRepository.save(destination);
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

                    destination.setTransactionStatus(Transfer.TransactionStatus.PROCESSING);
                    TransferOutgoingRequest request = new TransferOutgoingRequest();
                    request.setAccount(destination.getAccount());
                    request.setAction(MobilePayAction.DESTINATION);
                    request.setAmount(destination.getAmount());
                    request.setUserId(destination.getUserId());
                    request.setUserType(destination.getUserType());
                    request.setCharge(destination.getCharge());
                    request.setCurrency(destination.getCurrency());
                    request.setDescription(destination.getType().toString());
                    request.setExternalReference(transaction.getExternalReference());

                    ResponseObject<TransferIncomingResponse> response = null;
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
                            destination.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                            logger.debug("destination failed for card TODO implementation {}", destination);
                            break;
                        case BANK:
                            //TODO
                            destination.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                            logger.debug("destination failed for bank TODO implementation {}", destination);
                            break;
                    }

                    if (response != null) {
                        HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                        destination.setResponse(node);
                    }

                    if (response == null || ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                        destination.setTransactionStatus(Transfer.TransactionStatus.FAILED);
                    } else {
                        Transfer.TransactionStatus status =
                                ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                        Transfer.TransactionStatus.SUCCESS : Transfer.TransactionStatus.PROCESSING;
                        destination.setTransactionStatus(status);
                        logger.debug("destination ok  {} {}", destination, status);
                    }

                    destination = destinationRepository.save(destination);
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

    public ResponseObject<ChargeResponse> fetchCharge(ChargeRequest chargeRequest)
    {
        try {
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ChargeRequest>(chargeRequest, headers);

            ResponseEntity<ChargeResponse> response =
                    restTemplate.exchange(chargeUrl, HttpMethod.POST, entity, ChargeResponse.class);
            logger.debug(" {}", response);

            return new ResponseObject<ChargeResponse>(ResponseCode.SUCCESS.type, "success", null,
                    response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error(" {}", e);
            return null;
        }
    }

    public ResponseObject<String> generateTransactionId(AccountType accountType,
            Transfer.TransactionType transactionType,
            String countryCode)
    {
        try {
            logger.debug("generateTransactionId");
            TransactionIdRequest request = new TransactionIdRequest();
            request.setAccountType(accountType);
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

}
