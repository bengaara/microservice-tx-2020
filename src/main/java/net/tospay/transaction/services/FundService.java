package net.tospay.transaction.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.request.TopupMobileRequest;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.response.ChargeResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TopupMobileResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class FundService extends BaseService
{
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Autowired TransactionRepository transactionRepository;

    @Autowired SourceRepository sourceRepository;

    @Autowired DestinationRepository destinationRepository;

    @Value("${numbergenerator.transaction.url}")
    String numberGeneratorTransactionUrl;

    @Value("${charge.url}")
    String chargeUrl;

    @Value("${mobilepay.url}")
    private String mobilepayUrl;

    @Value("${walletpay.url}")
    private String walletpayUrl;

    public FundService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository)
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
            List<Source> sources = transactionRepository.findById(transaction.getId()).get()
                    .getSources();
            sources.forEach(source -> {

                switch (source.getType()) {
                    case MOBILE: //async
                    case WALLET://sync
                        source.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(source.getAccount());
                        request.setAction(MobilePayAction.SOURCE);
                        request.setAmount(source.getAmount());
                        request.setUserId(source.getUserId());
                        request.setUserType(source.getUserType());
                        request.setCharge(source.getCharge());
                        request.setCurrency(source.getCurrency());
                        request.setDescription(source.getType().toString());
                        request.setExternalReference(source.getId());

                        ResponseObject<TopupMobileResponse> response = null;
                        switch (source.getType()) {
                            case MOBILE: //async
                                response = hitMobile(request);
                                break;
                            case WALLET:
                                //process wallet response since its sync
                                response = hitWallet(request);
                                source.setDateResponse(new Timestamp(System.currentTimeMillis()));
                                break;
                        }

                        if (response != null) {
                            HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                            source.setResponse(node);
                        }
                        if (response == null || ResponseCode.FAILURE.type
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            source.setTransactionStatus(TransactionStatus.FAILED);
                            logger.debug("sourcing failed  {}", source);
                        } else {
                            logger.debug("sourcing ok  {}", source);
                        }
                        break;
                    case BANK:
                        break;
                    case CARD:
                        break;
                }

                sourceRepository.save(source);
            });
            checkSourceAndDestinationTransactionStatusAndAct(transaction);

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    public void checkSourceAndDestinationTransactionStatusAndAct(Transaction transaction)
    {
        //if all success - mark transaction source complete
        if (transaction == null) {
            logger.debug("null transaction ");
            return;
        }
        //only process incomplete transactions
        if (TransactionStatus.SUCCESS.equals(transaction.getTransactionStatus())
                || TransactionStatus.FAILED.equals(transaction.getTransactionStatus()))
        {
            logger.debug("cant process completed transaction  {}", transaction.getId());
            return;
        }

        AtomicBoolean sourcedSuccessAll = new AtomicBoolean(true);
        AtomicBoolean sourcedFail = new AtomicBoolean(false);
        transaction.getSources().forEach(s ->
        {
            sourcedSuccessAll
                    .set(sourcedSuccessAll.get() && TransactionStatus.SUCCESS.equals(s.getTransactionStatus()));
            if (TransactionStatus.FAILED.equals(s.getTransactionStatus())) {
                sourcedFail.set(true);
            }
        });
        logger.debug("mark transaction sourceComplete  {}  {}", transaction, sourcedSuccessAll);
        transaction.setSourceComplete(sourcedSuccessAll.get());

        if (sourcedSuccessAll.get() && sourcedFail.get()) {//if one failed

            logger.debug("mark transaction status  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            //TODO:actions
            //TODO : more actions? notify?
            logger.debug("TODO {}", transaction);
            return;
        } else if (transaction.isSourceComplete() && !transaction.isDestinationStarted()) {
            //fire destination
            logger.debug("source complete - firing payDestination  {}", transaction);
            transaction.setDestinationStarted(true);
            transactionRepository.save(transaction);
            CompletableFuture<Boolean> future = payDestination(transaction);
            return;
        } else {
            logger.debug("TODO unknown transaction source state  {}", transaction);
        }

        AtomicBoolean destinationSuccessAll = new AtomicBoolean(true);
        AtomicBoolean destinationFail = new AtomicBoolean(false);
        transaction.getDestinations().forEach(d ->
        {
            destinationSuccessAll
                    .set(destinationSuccessAll.get() && TransactionStatus.SUCCESS.equals(d.getTransactionStatus()));
        });
        logger.debug("mark transaction destinationComplete  {}  {}", transaction, destinationSuccessAll);
        transaction.setDestinationComplete(destinationSuccessAll.get());

        if (destinationSuccessAll.get() && destinationFail.get()) {//if one failed

            logger.debug("mark transaction status failed  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            //TODO: NOTIFY?what actions?
            logger.debug("TODO {}", transaction);
            return;
        } else if (destinationSuccessAll.get() && !destinationFail.get()) {//successful
            logger.debug("mark transaction status success  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            //TODO: NOTIFY?what actions?
            logger.debug("TODO  {}", transaction);
            return;
        } else {
            logger.debug("TODO unknown transaction destination state  {}", transaction);
        }

        // if all paydestination : fire notification
        if (transaction.isDestinationComplete()) {

            logger.debug("TODO: destinatio complete - firing notify  {}", transaction);
            //TODO: tell notify?
        }
    }

    public ResponseObject<TopupMobileResponse> hitMobile(TopupMobileRequest request)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TopupMobileRequest>(request, headers);

            ResponseObject<TopupMobileResponse> response =
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
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }

    public ResponseObject<TopupMobileResponse> hitWallet(TopupMobileRequest request)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TopupMobileRequest>(request, headers);

            ResponseObject<TopupMobileResponse> response =
                    restTemplate.postForObject(walletpayUrl, entity, ResponseObject.class);
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
            // Hibernate.initialize(transactionRepository);
            // transactionRepository.refresh(transaction);
            List<Destination> destinations = transactionRepository.findById(transaction.getId()).get()
                    .getDestinations();
            destinations.forEach(destination -> {

                switch (destination.getType()) {
                    case MOBILE:  //async wait for callback
                    case WALLET:

                        destination.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(destination.getAccount());
                        request.setAction(MobilePayAction.DESTINATION);
                        request.setAmount(destination.getAmount());
                        request.setUserId(destination.getUserId());
                        request.setUserType(destination.getUserType());
                        request.setCharge(destination.getCharge());
                        request.setCurrency(destination.getCurrency());
                        request.setDescription(destination.getType().toString());
                        request.setExternalReference(destination.getId());

                        ResponseObject<TopupMobileResponse> response = null;
                        switch (destination.getType()) {
                            case MOBILE:
                                response = hitMobile(request);
                                break;
                            case WALLET:
                                response = hitWallet(request);
                                destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
                                break;
                        }

                        if (response != null) {
                            HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
                            destination.setResponse(node);
                        }

                        if (response == null || !ResponseCode.SUCCESS.type
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            destination.setTransactionStatus(TransactionStatus.FAILED);
                        } else {
                            logger.debug("destination ok  {}", destination);
                        }
                        break;
                    case BANK:
                        break;
                    case CARD:
                        break;
                }
                destinationRepository.save(destination);
            });
            checkSourceAndDestinationTransactionStatusAndAct(transaction);

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

    public ResponseObject<String> fetchTransactionId(AccountType accountType, TransactionType transactionType,
            String countryCode)
    {
        try {

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
