//package net.tospay.transaction.services;
//
//import java.sql.Timestamp;
//import java.util.HashMap;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import net.tospay.transaction.entities.Destination;
//import net.tospay.transaction.entities.Transaction;
//import net.tospay.transaction.enums.AccountType;
//import net.tospay.transaction.enums.MobilePayAction;
//import net.tospay.transaction.enums.ResponseCode;
//import net.tospay.transaction.enums.TransactionStatus;
//import net.tospay.transaction.enums.TransactionType;
//import net.tospay.transaction.models.request.ChargeRequest;
//import net.tospay.transaction.models.request.TopupMobileRequest;
//import net.tospay.transaction.models.request.TransactionIdRequest;
//import net.tospay.transaction.models.response.ChargeResponse;
//import net.tospay.transaction.models.response.ResponseObject;
//import net.tospay.transaction.models.response.TopupMobileResponse;
//import net.tospay.transaction.repositories.DestinationRepository;
//import net.tospay.transaction.repositories.SourceRepository;
//import net.tospay.transaction.repositories.TransactionRepository;
//
//@Service
//public class NotifyService extends BaseService
//{
//    ObjectMapper mapper = new ObjectMapper();
//
//    @Autowired
//    RestTemplate restTemplate;
//
//    @Autowired TransactionRepository transactionRepository;
//
//    @Autowired SourceRepository sourceRepository;
//
//    @Autowired DestinationRepository destinationRepository;
//
//    @Value("${numbergenerator.transaction.url}")
//    String numberGeneratorTransactionUrl;
//
//    @Value("${charge.url}")
//    String chargeUrl;
//
//    @Value("${mobilepay.url}")
//    private String mobilepayUrl;
//
//    @Value("${walletpay.url}")
//    private String walletpayUrl;
//
//    public NotifyService(RestTemplate restTemplate, TransactionRepository transactionRepository,
//            SourceRepository sourceRepository, DestinationRepository destinationRepository)
//    {
//        this.restTemplate = restTemplate;
//
//        this.transactionRepository = transactionRepository;
//
//        this.sourceRepository = sourceRepository;
//
//        this.destinationRepository = destinationRepository;
//    }
//
//    public void notify(Transaction transaction)
//    {
//        try {
//            // Hibernate.initialize(transactionRepository);
//            List<Destination> destinations = transactionRepository.findById(transaction.getId()).get()
//                    .getDestinations();
//            destinations.forEach(destination -> {
//
//                switch (destination.getType()) {
//                    case MOBILE:  //async wait for callback
//                    case WALLET:
//
//                        destination.setTransactionStatus(TransactionStatus.PROCESSING);
//                        TopupMobileRequest request = new TopupMobileRequest();
//                        request.setAccount(destination.getAccount());
//                        request.setAction(MobilePayAction.DESTINATION);
//                        request.setAmount(destination.getAmount());
//                        request.setUserId(destination.getUserId());
//                        request.setUserType(destination.getUserType());
//                        request.setCharge(destination.getCharge());
//                        request.setCurrency(destination.getCurrency());
//                        request.setDescription(destination.getType().toString());
//                        request.setExternalReference(destination.getId());
//
//                        ResponseObject<TopupMobileResponse> response = null;
//                        switch (destination.getType()) {
//                            case MOBILE:
//                                response = hitMobile(request);
//                                break;
//                            case WALLET:
//                                response = hitWallet(request);
//                                destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
//                                break;
//                        }
//
//                        if (response != null) {
//                            HashMap<String, Object> node = mapper.convertValue(response, HashMap.class);
//                            destination.setResponse(node);
//                        }
//
//                        if (response == null || !ResponseCode.SUCCESS.code
//                                .equalsIgnoreCase(response.getStatus()))
//                        {//failure
//                            destination.setTransactionStatus(TransactionStatus.FAILED);
//                            destination.getTransaction().setTransactionStatus(TransactionStatus.FAILED);
//                        } else {
//                            logger.debug("destination ok", destination);
//                        }
//
//                        //if all success - mark transaction success
//                        AtomicBoolean destinationAll = new AtomicBoolean(true);
//                        AtomicBoolean destinationCompleteAll = new AtomicBoolean(true);
//                        destination.getTransaction().getDestinations().forEach(d ->
//                        {
//                            destinationAll
//                                    .set(destinationAll.get() && TransactionStatus.SUCCESS == d
//                                            .getTransactionStatus());
//                            destinationCompleteAll
//                                    .set(destinationCompleteAll.get() && (
//                                            TransactionStatus.SUCCESS == d.getTransactionStatus()
//                                                    || TransactionStatus.FAILED == d.getTransactionStatus()));
//                        });
//
//                        if (destinationAll.get()) {
//                            //get transaction id
//                            ResponseObject<String> res =
//                                    fetchTransactionId(destination.getTransaction().getAccountType(),
//                                            destination.getTransaction().getTransactionType(),
//                                            destination.getTransaction().getCountryCode());
//                            if (res != null && ResponseCode.SUCCESS.code.equalsIgnoreCase(res.getStatus())) {
//                                destination.getTransaction().setTransactionId(res.getData());
//                            }
//
//                            destination.getTransaction().setTransactionStatus(TransactionStatus.SUCCESS);
//                        } else if (destinationCompleteAll.get()) {
//                            logger.debug("", "failing the transaction", destination.getTransaction());
//                            destination.getTransaction().setTransactionStatus(TransactionStatus.FAILED);
//                        }
//
//                        destinationRepository.save(destination);
//                        transactionRepository.save(destination.getTransaction());
//
//                        break;
//                    case BANK:
//                        break;
//                    case CARD:
//                        break;
//                }
//
//                destinationRepository.save(destination);
//            });
//
//            return CompletableFuture.completedFuture(true);
//        } catch (Exception e) {
//            logger.error("", e);
//            return CompletableFuture.completedFuture(false);
//        }
//    }
//
//    public ResponseObject<ChargeResponse> fetchCharge(ChargeRequest chargeRequest)
//    {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity entity = new HttpEntity<ChargeRequest>(chargeRequest, headers);
//
//            ResponseEntity<ChargeResponse> response =
//                    restTemplate.exchange(chargeUrl, HttpMethod.POST, entity, ChargeResponse.class);
//            logger.debug("", response);
//
//            return new ResponseObject<ChargeResponse>(ResponseCode.SUCCESS.code, "success", null,
//                    response.getBody());
//        } catch (HttpClientErrorException e) {
//            logger.error("", e.getResponseBodyAsString());
//            return null;
//        } catch (Exception e) {
//            logger.error("", e);
//            return null;
//        }
//    }
//
//    public ResponseObject<String> fetchTransactionId(AccountType accountType, TransactionType transactionType,
//            String countryCode)
//    {
//        try {
//
//            TransactionIdRequest request = new TransactionIdRequest();
//            request.setAccountType(accountType);
//            request.setCountry(countryCode);
//            request.setTransactionType(transactionType);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HttpEntity entity = new HttpEntity<TransactionIdRequest>(request, headers);
//
//            ResponseObject<String> response =
//                    restTemplate.postForObject(numberGeneratorTransactionUrl, entity, ResponseObject.class);
//            logger.debug("", response);
//
//            return response;
//        } catch (HttpClientErrorException e) {
//            logger.error("", e.getResponseBodyAsString());
//            return null;
//        } catch (Exception e) {
//            logger.error("", e);
//            return null;
//        }
//    }
//}
