package net.tospay.transaction.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.AsyncCallbackResponse;
import net.tospay.transaction.models.CardOrderInfo;
import net.tospay.transaction.models.ChargeInfo;
import net.tospay.transaction.models.MerchantInfo;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.request.PaymentRequest;
import net.tospay.transaction.models.request.PaymentSplitResponse;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class FundService extends BaseService
{
    @Autowired
    RestTemplate restTemplate;

    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    @Autowired
    NotifyService notifyService;

    @Autowired
    CrudService crudService;

    @Value("${numbergenerator.transaction.url}")
    String numberGeneratorTransactionUrl;

    @Value("#{${STORE_PAY_URLS}}")
    Map<String, String> STORE_PAY_URLS;

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
            List<Source> sources = transaction.getSources();
            sources.forEach(source -> {

                source.setTransactionStatus(TransactionStatus.PROCESSING);
                TransferOutgoingRequest request = new TransferOutgoingRequest();
                request.setAccount(source.getPayload().getAccount());
                request.setAction("SOURCE");
                request.setAmount(source.getPayload().getTotal());
                request.setExternalReference(source.getId());
                request.setDescription("Source Pay");

                ResponseObject<StoreResponse> response = null;
                //TODO. hack for card
                if (AccountType.CARD.equals(source.getPayload().getAccount().getType())) {
                    request.setDeviceInfo(transaction.getPayload().getDeviceInfo());
                    request.setUserInfo(transaction.getPayload().getUserInfo());
                    request.setMerchantInfo(transaction.getPayload().getMerchantInfo());
                    request.setOrderInfo(CardOrderInfo.from(source));
                    response = hitStore(source.getPayload().getAccount().getType(), request);
                } else {
                    response = hitStore(source.getPayload().getAccount().getType(), request);
                }

                source.getResponse().put(LocalDateTime.now(), response.getData());
                if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                    source.setTransactionStatus(TransactionStatus.FAILED);
                    logger.error("sourcing failed  {}", source);
                } else {

                    TransactionStatus status =
                            ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                    TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
                    source.setTransactionStatus(status);
                    logger.debug("sourcing ok  {} {}", source, status);

                    //one success.. generate TR id
                    if (source.getTransaction().getTransactionId() == null) {

                        //    Account merchantInfo = source.getTransaction().getPayload().getDelivery().get(0).getAccount();
                        UserInfo userInfo = source.getTransaction().getPayload().getUserInfo();
                        ResponseObject<String> tr = generateTransactionId(userInfo.getTypeId(),
                                source.getTransaction().getPayload().getType(), userInfo.getCountry().getIso());
                        if (tr != null && ResponseCode.SUCCESS.type.equals(tr.getStatus())) {
                            logger.debug("transaction id {}", tr.getData());
                            source.getTransaction().setTransactionId(tr.getData());
                        }
                    }
                }

                // sourceRepository.save(source);
                //transactionRepository.saveAndFlush(transaction);
            });
            transactionRepository.saveAndFlush(transaction);
            //transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
            checkSourceAndDestinationTransactionStatusAndAct(sources.get(0).getTransaction());

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    //refunds go to wallet so its fast... make transactional
    @Transactional
    public void refundFloatingFundsToWallet(net.tospay.transaction.entities.Transaction transaction)
    {
        if (transaction == null) {
            logger.debug("refundFloatingFundsToWallet no transaction");
            return;
        }

        if (!TransactionStatus.FAILED.equals(transaction.getTransactionStatus())) {
            logger.debug("no refund for transaction {} status {}", transaction.getId(),
                    transaction.getTransactionStatus());
            return;
        }

        BigDecimal amount = BigDecimal.ZERO;
        for (Source source : transaction.getSources()) {
            if (TransactionStatus.SUCCESS.equals(source.getTransactionStatus())) {
                amount = amount.add(source.getPayload().getOrder().getAmount());//TODO transaction cost je? zirudi?
                logger.debug("adding sourced amount {} ", source.getPayload().getTotal().getAmount());
            }
        }
        for (Destination destination : transaction.getDestinations()) {
            if (TransactionStatus.SUCCESS.equals(destination.getTransactionStatus())) {
                amount = amount.subtract(destination.getPayload().getTotal().getAmount());
                logger.debug("deducting delivered amount {} ", destination.getPayload().getTotal().getAmount());
            }
        }
        if (amount.compareTo(BigDecimal.ZERO) != 1) {
            logger.debug("cant refund transaction amount {} orderinfo {} transaction {}",
                    amount, transaction.getPayload().getOrderInfo().getAmount().getAmount(), transaction.getId());
            return;
        }

        //start refund

        Source source = transaction.getSources().get(0);
        logger.debug("refunding transaction {} amount {}", transaction.getId(), amount);

        Amount total = new Amount(amount,
                transaction.getPayload().getOrderInfo().getAmount().getCurrency());
        Store store = new Store();
        store.setAccount(source.getPayload()
                .getAccount());//NB no transaction has 2 different users.. so single source gives us recipient
        store.getAccount().setUserId(source.getTransaction().getUserInfo().getUserId());
        store.getAccount().setUserType(source.getTransaction().getUserInfo().getTypeId());
        store.setTotal(total);

        Destination destination = new Destination();
        destination.setTransaction(transaction);
        transaction.addDestination(destination);
        destination.setPayload(store);
        destination.setTransactionStatus(TransactionStatus.CREATED);
        destinationRepository.save(destination);

        TransferOutgoingRequest request = new TransferOutgoingRequest();
        request.setAccount(store.getAccount());
        request.setAction("DESTINATION");
        request.setAmount(store.getTotal());
        request.setExternalReference(source.getId());
        request.setDescription("REFUND");

        ResponseObject<StoreResponse> response = hitStore(AccountType.WALLET, request);

        if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
            destination.setTransactionStatus(TransactionStatus.FAILED);
        } else {
            destination.setTransactionStatus(TransactionStatus.REVERSED);
            transaction.setTransactionStatus(TransactionStatus.REVERSED);
            transaction.setDateRefunded(LocalDateTime.now());
            logger.debug("destination ok transaction status : {}  {} {}", transaction.getTransactionStatus(),
                    destination, TransactionStatus.REVERSED);
        }

        transaction.setRefundRetryCount(transaction.getRefundRetryCount() + 1);
        destinationRepository.save(destination);
        transactionRepository.saveAndFlush(transaction);
    }

    public void checkSourceAndDestinationTransactionStatusAndAct(Transaction transaction)
    {

        ObjectMapper objectMapper = new ObjectMapper();
        //if all success - mark transaction source complete
        if (transaction == null) {
            logger.debug("null transaction ");
            return;
        }
        transaction = transactionRepository.findById(transaction.getId()).get();
        //  transactionRepository.refresh(transaction);
        //only process incomplete transactions
        if (!Arrays.asList(TransactionStatus.PROCESSING, TransactionStatus.CREATED)
                .contains(transaction.getTransactionStatus()))
        {
            logger.debug("cant process completed transaction  {} {}", transaction.getId(),
                    transaction.getTransactionStatus());
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
            notifyService.notifySource(transaction);
            //auto rollback funds to wallet?
            refundFloatingFundsToWallet(transaction);

            //check other failed ones:
            LocalDateTime midnight = LocalDate.now().atStartOfDay();
            List<Transaction> list = crudService.fetchFailedSourcedTransactions(midnight);

            list.forEach(t -> {
                refundFloatingFundsToWallet(t);
            });

            return;
        } else if (transaction.isSourceComplete() && !transaction.isDestinationStarted()) {
            //fire destination
            logger.debug("source complete - firing payDestination  {}", transaction);
            transaction.setDestinationStarted(true);
            transactionRepository.saveAndFlush(transaction);

            //TODO:actions
            logger.debug("TODO {}", transaction);
            //notify
            notifyService.notifySource(transaction);

            CompletableFuture<Boolean> future = payDestination(transaction);

            return;
        } else {
            logger.debug("transaction status :{}  {}", transaction.getId(), transaction.getTransactionStatus());
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

            notifyService.notifyDestination(transaction);

            //auto rollback funds to wallet?
            refundFloatingFundsToWallet(transaction);

            return;
        } else if (destinationSuccessAll.get() && !destinationFail.get()) {//successful
            logger.debug("mark transaction status success  {}", transaction);
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            transactionRepository.saveAndFlush(transaction);

            //TODO:what actions?
            logger.debug("TODO destinations success {}", transaction);
            notifyService.notifyDestination(transaction);

            return;
        } else {
            logger.debug("TODO transaction status  {} {}", transaction.getId(), transaction.getTransactionStatus());
        }

        //if payment transaction notify paymentservice
        if (TransactionType.PAYMENT.equals(transaction.getPayload().getType())) {
            hitPaymentPayService(transaction);
        }
    }

    public ResponseObject<PaymentSplitResponse> hitPaymentSplitService(
            net.tospay.transaction.entities.Transaction transaction)
    {

        try {
            logger.debug("hitSplitPaymentService {}", transaction);

            PaymentRequest request = new PaymentRequest();
            request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchant(transaction.getPayload().getDelivery().get(0).getAccount().getUserId());
            request.setReference(transaction.getPayload().getOrderInfo().getReference());
            request.setTransactionId(transaction.getId().toString());
            request.setStatus(transaction.getTransactionStatus());

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentRequest>(request, headers);

            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject("splitPaymentUrl", entity, ResponseObject.class);
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

    public ResponseObject<PaymentSplitResponse> hitPaymentPayService(
            net.tospay.transaction.entities.Transaction transaction)
    {

        try {
            logger.debug("hitPaymentPayService {}", transaction);
            String url = null;
            OrderType orderType = transaction.getPayload().getOrderInfo().getType();
            url = STORE_PAY_URLS.get(orderType);

            PaymentRequest request = new PaymentRequest();
            request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchant(transaction.getPayload().getDelivery().get(0).getAccount().getUserId());
            request.setReference(transaction.getPayload().getOrderInfo().getReference());
            request.setTransactionId(transaction.getId().toString());
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

    public ResponseObject<StoreResponse> hitStore(AccountType accountType, TransferOutgoingRequest request)
    {
        try {
            String url = STORE_PAY_URLS.get(accountType.name());
            logger.debug("request: {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            ResponseObject<StoreResponse> response =
                    restTemplate.postForObject(url, entity, ResponseObject.class);
            logger.debug("response {}", response);

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
            logger.error(" {}", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getLocalizedMessage();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    @Async
    @Transactional
    public CompletableFuture<Boolean> payDestination(final net.tospay.transaction.entities.Transaction transaction)
    {
        try {

            //if split bill enquire if sourcing complete
            if (TransactionType.PAYMENT.equals(transaction.getPayload().getType())
                    && OrderType.SPLIT.equals(transaction.getPayload().getOrderInfo().getType()))
            {
                ResponseObject<PaymentSplitResponse> res = hitPaymentSplitService(transaction);

                //if split complete pay merchant wallet
                if (ResponseCode.SUCCESS.type.equalsIgnoreCase(res.getStatus()) && res.getData() != null
                        && res.getData().isPay())
                {
                    PaymentSplitResponse response = res.getData();

                    Transaction tran = new Transaction();
                    tran.setPayload(null);//TODO payload null when internal request?
                    tran.setTransactionStatus(TransactionStatus.CREATED);

                    Source sourceEntity = new Source();
                    Store storeSource = new Store();
                    storeSource.setAccount(response.getAccount());
                    storeSource.setTotal(response.getAmount());
                    sourceEntity.setPayload(storeSource);
                    sourceEntity.setTransactionStatus(TransactionStatus.CREATED);
                    tran.addSource(sourceEntity);
                    sourceEntity.setTransaction(tran);

                    Destination destinationEntity = new Destination();
                    Store storeDest = new Store();
                    storeDest.setAccount(response.getAccount());
                    storeDest.setTotal(response.getAmount());
                    destinationEntity.setPayload(storeDest);
                    destinationEntity.setTransactionStatus(TransactionStatus.CREATED);
                    tran.addDestination(destinationEntity);

                    transactionRepository.saveAndFlush(tran);
                    //trigger paying merchant async
                    processDestinations(tran);
                } else {
                    logger.debug("split bill sourcing not complete yet {}", transaction.getId());
                }
            } else {
                processDestinations(transaction);
            }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("{}", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    void processDestinations(final net.tospay.transaction.entities.Transaction transaction)
    {

        List<Destination> destinations = transaction
                .getDestinations();//transactionRepository.findById(transaction.getId()).get().getDestinations();
        destinations.forEach(destination -> {

            destination.setTransactionStatus(TransactionStatus.PROCESSING);

            TransferOutgoingRequest request = new TransferOutgoingRequest();
            request.setAccount(destination.getPayload().getAccount());
            request.setAction("DESTINATION");
            request.setAmount(destination.getPayload().getTotal());
            request.setExternalReference(destination.getId());
            request.setDescription("Deliver funds");

            ResponseObject<StoreResponse> response = null;

            //TODO. hack for card
            if (AccountType.CARD.equals(destination.getPayload().getAccount().getType())) {
                request.setDeviceInfo(transaction.getPayload().getDeviceInfo());
                request.setUserInfo(transaction.getPayload().getUserInfo());
                request.setMerchantInfo(transaction.getPayload().getMerchantInfo());

                request.setOrderInfo(CardOrderInfo.from(destination));
                response = hitStore(destination.getPayload().getAccount().getType(), request);
            } else {
                response = hitStore(destination.getPayload().getAccount().getType(), request);
            }

            destination.getResponse().put(LocalDateTime.now(), response.getData());

            if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                destination.setTransactionStatus(TransactionStatus.FAILED);
            } else {
                TransactionStatus status =
                        ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ?
                                TransactionStatus.SUCCESS : TransactionStatus.PROCESSING;
                destination.setTransactionStatus(status);
                logger.debug("destination ok  {} {}", destination, status);
            }

            //java-util-concurrentmodificationexception for(int i = 0; i<myList.size(); i++){
            //destinationRepository.save(destination);
            // transactionRepository.save(transaction);
        });

        //pay merchants and partner
        ChargeInfo chargeInfo = transaction.getPayload().getChargeInfo();
        if (chargeInfo.getFx().getAmount().getAmount().compareTo(BigDecimal.ZERO) == 1) {
            logger.debug("saving  FX revenue  {} {}", chargeInfo.getFx().getAmount().getAmount(),
                    chargeInfo.getFx().getAccount().getUserId());
            payPartners(transaction, chargeInfo.getFx().getAccount(), chargeInfo.getFx().getAmount());
        }
        if (chargeInfo.getRailInfo().getAmount().getAmount().compareTo(BigDecimal.ZERO) == 1) {
            logger.debug("saving  Rail revenue  {} {}", chargeInfo.getRailInfo().getAmount().getAmount(),
                    chargeInfo.getRailInfo().getAccount().getUserId());
            payPartners(transaction, chargeInfo.getRailInfo().getAccount(), chargeInfo.getRailInfo().getAmount());
        }
        if (chargeInfo.getPartnerInfo().getAmount().getAmount().compareTo(BigDecimal.ZERO) == 1) {
            logger.debug("saving  Partner revenue  {} {}", chargeInfo.getPartnerInfo().getAmount().getAmount(),
                    chargeInfo.getPartnerInfo().getAccount().getUserId());
            payPartners(transaction, chargeInfo.getPartnerInfo().getAccount(), chargeInfo.getPartnerInfo().getAmount());
        }

        transactionRepository.saveAndFlush(transaction);
        // transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
        checkSourceAndDestinationTransactionStatusAndAct(destinations.get(0).getTransaction());
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

    //NB: External Reference here is UUID of Source or Destination.
    public boolean processPaymentCallback(AsyncCallbackResponse response)

    {
        try {
            Optional<Source> optionalSource = response.getExternalReference() == null ? Optional.empty() :
                    sourceRepository.findById(response.getExternalReference());
            Optional<net.tospay.transaction.entities.Destination> optionalDestination =
                    response.getExternalReference() == null ? Optional.empty() :
                            destinationRepository.findById(response.getExternalReference());

            Transaction transaction = null;
            if (optionalSource.isPresent()) {
                net.tospay.transaction.entities.Source source = optionalSource.get();
                transaction = source.getTransaction();
                source.setTransactionStatus(response.getTransaction().getStatus());
                source.getResponseAsync().put(LocalDateTime.now(), response);
                sourceRepository.save(source);
            } else if (optionalDestination.isPresent()) {
                net.tospay.transaction.entities.Destination source = optionalDestination.get();
                transaction = source.getTransaction();
                source.setTransactionStatus(response.getTransaction().getStatus());
                source.getResponseAsync().put(LocalDateTime.now(), response);
                destinationRepository.save(source);
            } else {
                //TODO: callback from where?
                logger.error("callback called but no Record found {}", response);
            }
            checkSourceAndDestinationTransactionStatusAndAct(transaction);
            return true;
        } catch (Exception e) {
            logger.error(" {}", e);
            return false;
        }
    }

    void payPartners(Transaction transaction, Account account, Amount amount)
    {

        logger.debug("revenue for transaction {} user {} amount {}", transaction.getId(), account.getUserId(), amount);

        Store store = new Store();
        store.setAccount(account);
        store.setTotal(amount);

        //A revenue has a new destination added to the transaction leg
        Destination destination = new Destination();
        destination.setTransaction(transaction);
        transaction.addDestination(destination);
        destination.setPayload(store);
        destination.setTransactionStatus(TransactionStatus.CREATED);
        destinationRepository.save(destination);

        TransferOutgoingRequest request = new TransferOutgoingRequest();
        request.setAccount(store.getAccount());
        request.setAction("DESTINATION");
        request.setAmount(store.getTotal());
        request.setExternalReference(destination.getId());
        request.setDescription("REVENUE");

        ResponseObject<StoreResponse> response = hitStore(AccountType.WALLET, request);

        if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
            destination.setTransactionStatus(TransactionStatus.FAILED);
        } else {
            destination.setTransactionStatus(TransactionStatus.SUCCESS);
            logger.debug("charges/revenue ok transaction status : {}  {}", transaction.getTransactionStatus(),
                    destination);
        }
        destinationRepository.save(destination);
        //transactionRepository.saveAndFlush(transaction);
    }
}
