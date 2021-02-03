package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.tospay.transaction.entities.BaseSource;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.FraudStatus;
import net.tospay.transaction.enums.OrderType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.StoreActionType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UtilityType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.AsyncCallbackResponse;
import net.tospay.transaction.models.ChargeInfo;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.models.StoreStatusResponse;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.request.PaymentRequest;
import net.tospay.transaction.models.request.PaymentSplitResponse;
import net.tospay.transaction.models.request.ReversalOutgoingRequest;
import net.tospay.transaction.models.request.SettlementOutgoingRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionLimit;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.util.Constants.URL;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class FundService extends BaseService {


    @Autowired //TODO bug dont remove - when method is called asyn they r needed
            TransactionRepository transactionRepository;
    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DestinationRepository destinationRepository;

    @Autowired
    NotifyService notifyService;

    @Autowired
    CrudService crudService;

    @Value("#{${STORE_PAY_URLS}}")
    Map<String, String> STORE_PAY_URLS;

    @Value("#{${STORE_STATUS_URLS}}")
    Map<String, String> STORE_STATUS_URLS;

    @Value("${paymentpay.url}")
    String paymentUrl;

    @Value("${settlement.url}")
    String settlementUrl;

    @Value("${reversal.callback.url}")
    String reversalCallbackUrl;
    @Value("${server.url}")
    String serverUrl;

    @Autowired
    NumberGeneratorService numberGeneratorService;

    CommissionService commissionService;

    TransactionLimitService transactionLimitService;

    ForexService forexService;

    DashboardService dashboardService;

    ReversalService reversalService;


    public FundService(RestTemplate restTemplate, TransactionRepository transactionRepository,
                       SourceRepository sourceRepository, DestinationRepository destinationRepository, NotifyService notifyService
            , NumberGeneratorService numberGeneratorService,CommissionService commissionService,
        TransactionLimitService transactionLimitService,ForexService forexService,DashboardService dashboardService,ReversalService reversalService) {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;

        this.numberGeneratorService = numberGeneratorService;

        this.commissionService =  commissionService;

        this.transactionLimitService = transactionLimitService;

        this.forexService =forexService;

        this.dashboardService = dashboardService;

        this.reversalService =reversalService;

    }


    @Async
    // @Transactional - dont do transactional
    public CompletableFuture<Pair<Transaction, List<String>>> pullFromSource(net.tospay.transaction.entities.Transaction transaction) {


        logger.debug("pullFromSource {}", transaction.getId());
        List<String> list = new ArrayList();
        try {

// Hibernate.initialize(transactionRepository);transactionRepository.refresh(transaction);

            transaction.setTransactionStatus(TransactionStatus.PROCESSING);
            //if payment transaction notify paymentservice
            logger.debug("check TransactionType {} {} {}", transaction.getTransactionId(), transaction.getType(), transaction.getTransactionStatus());
            if (TransactionType.PAYMENT.equals(transaction.getType())) {
                logger.debug("hitPaymentPayService for TransactionType.PAYMENT id {} reference {}", transaction.getId(), transaction.getPayload().getOrderInfo().getReference());
                hitPaymentPayService(transaction);
            }

            //   List<Source> sources = transactionRepository.findById(transaction.getId()).get().getSources();
            List<Source> sources = transaction.getSources();
            this.logger.debug("sources count {}", sources.size());
            sources.forEach(source -> {

                source.setTransactionStatus(TransactionStatus.PROCESSING);
                TransferOutgoingRequest request = new TransferOutgoingRequest();
                request.setAccount((Account) Utils.deepCopy(source.getPayload().getAccount()));
                request.setTransactionType(source.getTransaction().getType());
                request.setMerchantReference(source.getTransaction().getPayload().getOrderInfo().getReference());

                //TODO: hack remove later for moses  and his strict mode

                if (AccountType.WALLET.equals(source.getPayload().getAccount().getType())) {
                    request.getAccount().setCountry(null);
                    request.setMerchantReference(null);
                }
                if (AccountType.MOBILE.equals(source.getPayload().getAccount().getType())) {
                    request.setMerchantReference(null);
                }

                //TODO: hack remove later for moses  and his strict mode. check if uuid not phone number.. so no need 4 country object
                String[] var1 = source.getPayload().getAccount().getId().split("-");//iss uuid
                if (AccountType.MOBILE.equals(source.getPayload().getAccount().getType()) &&
                        var1.length > 2) {
                    request.getAccount().setCountry(null);
                }

                if (Arrays.asList(AccountType.WALLET, AccountType.MOBILE)
                    .contains(source.getPayload().getAccount().getType())) {
                    logger.debug("clear out fields thanks to mose strict mode :-( {}",
                        source.getTransaction().getId());
                    request.getAccount().setName(null);
                    request.getAccount().setPhone(null);
                    request.getAccount().setEmail(null);
                    request.setTransactionType(null);
                }
                request.setAction("SOURCE");
                request.setAmount(source.getPayload().getTotal());
                request.setExternalReference(source.getId());
                request.setDescription(
                    source.getTransaction().getPayload().getOrderInfo().getDescription());
                source.getRequest().put(LocalDateTime.now(), request);

                ResponseObject<StoreResponse> response;
                //SYSTEM_LOAD has no source
                if (OrderType.SYSTEM_LOAD
                    .equals(transaction.getPayload().getOrderInfo().getType())) {
                    logger.debug(" OrderType.SYSTEM_LOAD - fake source skip sourcing");
                    response = new ResponseObject<>();
                    response.setStatus(ResponseCode.SUCCESS.type);

                } else {
                    response = hitStore(source.getPayload().getAccount().getType(), request);
                }

                source.getResponse().put(LocalDateTime.now(),response);
                source.setStoreRef(
                    response.getData() != null ? response.getData().getStoreRef() : null);
                source.setAvailableBalance(
                    response.getData() != null ? response.getData().getNewBalance() : null);
                source.setFxId(response.getData() != null ? response.getData().getFxId() : null);
                if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
                    source.setTransactionStatus(TransactionStatus.FAILED);
                    if (response.getError() != null) {
                        source.setCode(response.getError().get(0).getCode());
                        source.setReason(response.getError().get(0).getDescription());
                    }
                } else if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())) {
                    source.setTransactionStatus(TransactionStatus.SUCCESS);
                    source.setCode(ResponseCode.SUCCESS.type);
                    source.setReason(ResponseCode.SUCCESS.name());
                }
                if (response.getData() != null && response.getData().getHtml() != null) {
                    list.add(response.getData().getHtml());
                }

                logger.error("destination store hit  {} {}", source.getId(), source.getTransactionStatus());
                //one success.. generate TR id
                if (!TransactionType.REVERSAL.equals(source.getTransaction().getType()) && Arrays.asList(TransactionStatus.SUCCESS,TransactionStatus.PROCESSING).contains(source.getTransactionStatus()) && source.getTransaction().getTransactionId() == null) {
                    numberGeneratorService.generateTransactionId(source.getTransaction());//TODO: bad designs. propagates changes to transaction
                }


                // sourceRepository.save(source);
                //transactionRepository.saveAndFlush(transaction);

            });
            //transactionRepository.save(transaction);
            //transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
            this.transactionRepository.saveAndFlush(transaction);
            processTransactionStatus(transaction, null);

            return CompletableFuture.completedFuture(Pair.of(transaction, list));
        } catch (Exception e) {
            logger.error("", e);
            processTransactionStatus(transaction, e);
            return CompletableFuture.completedFuture(Pair.of(transaction, list));

        }
    }


    public void processTransactionStatus(Transaction t, Exception e) {

        this.logger.debug("processTransactionStatus {} {} {}", t.getId(), e);
        Transaction transaction = t;// this.transactionRepository.findById(t.getId()).get();
        if (e != null) {
            try {
                transaction.setException(objectMapper.writeValueAsString(e));
            } catch (JsonProcessingException jsonProcessingException) {
                jsonProcessingException.printStackTrace();
            }
        }
        //  transactionRepository.refresh(transaction);
        //only process incomplete transactions
        if (!Arrays.asList(TransactionStatus.PROCESSING, TransactionStatus.CREATED)
            .contains(t.getTransactionStatus())) {
            logger.debug("cant process completed transaction  {} {}", t.getId(),
                t.getTransactionStatus());
            return;
        }

        //check if fraud marked it as dont proceed on continue 2nd leg
        if (transaction.getFraudInfo() != null && FraudStatus.DO_NOT_PROCEED.equals(transaction.getFraudInfo().getStatus())) {
            if(!transaction.getSources().isEmpty()) {
                transaction.getSources().get(0).setTransactionStatus(TransactionStatus.FAILED);
                transaction.getSources().get(0).setCode(transaction.getFraudInfo().getStatusCode());
                transaction.getSources().get(0).setReason(transaction.getFraudInfo().getReason());
            }
            transaction.setCode(transaction.getFraudInfo().getStatusCode());
            transaction.setReason(transaction.getFraudInfo().getReason());
            transaction.setTransactionStatus(TransactionStatus.FAILED);

            transactionRepository.save(transaction);
            //notify
            notifyService.notifySource(transaction);
            logger.debug("return from processTransactionStatus  {}", transaction.getId());

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

                logger.debug("failed source {} {}", s.getId(), transaction.getId());
                transaction.setTransactionStatus(TransactionStatus.FAILED);
                transaction.setCode(s.getCode());
                transaction.setReason(s.getReason());
                transactionRepository.save(transaction);
                return; //for exiting foreach loop
            }
        });
        logger.debug("mark transaction sourceComplete  {}  {}", transaction.getId(), sourcedSuccessAll);
        transaction.setSourceComplete(sourcedSuccessAll.get());

        if (sourcedFail.get()) {//if one failed

            //notify
            notifyService.notifySource(transaction);
            logger.debug("return from checkstatus  {}", transaction.getId());

            Transaction reversalTransaction = reversalService.reverseTransaction(transaction, null);
            //trigger sourcing async
            if(reversalTransaction !=null) {
                CompletableFuture<Pair<Transaction, List<String>>> future = this
                    .pullFromSource(reversalTransaction);
            }

            return;
        } else if (transaction.isSourceComplete() && !transaction.isDestinationStarted()) {
            //fire destination
            logger.debug("source complete - firing payDestination  {}", transaction.getId());
            transaction.setDestinationStarted(true);
            CompletableFuture<Boolean> future = pushToDestination(transaction);

            return;
        } else {
            logger.error("sourcing not complete:  transaction status :{}  {} destinationStatus: {} ", transaction.getId(), transaction.getTransactionStatus(), transaction.isDestinationStarted());
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

                logger.debug("failed destination {} {}", d.getId(), transaction.getId());
                transaction.setTransactionStatus(TransactionStatus.FAILED);
                transaction.setCode(d.getCode());
                transaction.setReason(d.getReason());
                transactionRepository.save(transaction);
                return;
            }
        });
        logger.debug("mark transaction destination Complete  {}  {}", transaction.getId(), destinationSuccessAll);

        transaction.setDestinationComplete(destinationSuccessAll.get());

       if (destinationSuccessAll.get() ) {//TODO:  bug assumes 1 delivery only - sets successful
            transaction.setTransactionStatus(TransactionStatus.SUCCESS);
            logger.debug("mark transaction status success  {}", transaction.getId());
            transaction.setCode(ResponseCode.SUCCESS.type);
            transaction.setReason(ResponseCode.SUCCESS.name());

            transactionRepository.save(transaction);

            //TODO:what actions?
            logger.debug("TODO destinations success {}", transaction.getId());


            //  return;
        } else {
            logger.error("TODO transaction status  {} {} {} {} ", transaction.getId(), transaction.getTransactionStatus(), transaction.isDestinationStarted(), transaction.isDestinationComplete());
        }

        //notify after success/failure
        notifyService.notifySource(transaction);
        notifyService.notifyDestination(transaction);

        if (destinationFail.get()) {//if one failed


            Transaction reversalTransaction = reversalService.reverseTransaction(transaction, null);
            //trigger sourcing async
            if(reversalTransaction !=null) {
                CompletableFuture<Pair<Transaction, List<String>>> future = this
                    .pullFromSource(reversalTransaction);
                // return;
            }
        }

            //if payment transaction notify paymentservice
        logger.debug("check TransactionType {} {} {}", transaction.getTransactionId(), transaction.getType(), transaction.getTransactionStatus());
        if (TransactionType.PAYMENT.equals(transaction.getType())) {
            logger.debug("hitPaymentPayService for TransactionType.PAYMENT {} {}", transaction.getTransactionId(), transaction.getPayload().getOrderInfo().getReference());
            hitPaymentPayService (transaction);
        }


        //if one destination fails, dont process revenue
        //Process Revenue: pay merchants and partner
        if (!TransactionType.REVERSAL.equals(transaction.getType()) && TransactionStatus.SUCCESS.equals(transaction.getTransactionStatus()) && !transaction.isRevenueProcessed()) {
            logger.debug("Process Revenue {} {}", transaction.getTransactionId(), transaction.getTransactionStatus());
            ChargeInfo chargeInfo = transaction.getPayload().getChargeInfo();

            if (chargeInfo.getRailInfo() != null && chargeInfo.getRailInfo().getAmount()!=null && chargeInfo.getRailInfo().getAmount().getAmount().compareTo(BigDecimal.ZERO) == 1) {
                logger.debug("saving  Rail revenue  {} {}", chargeInfo.getRailInfo().getAmount().getAmount(),
                        chargeInfo.getRailInfo().getAccount().getUserId());
                payPartners(transaction, chargeInfo.getRailInfo().getAccount(), chargeInfo.getRailInfo().getAmount());
            }
            if (chargeInfo.getPartnerInfo() != null && chargeInfo.getPartnerInfo().getAmount()!=null && chargeInfo.getPartnerInfo().getAmount().getAmount().compareTo(BigDecimal.ZERO) == 1) {
                logger.debug("saving  Partner revenue  {} {}",
                    chargeInfo.getPartnerInfo().getAmount().getAmount(),
                    chargeInfo.getPartnerInfo().getAccount().getUserId());
                TransactionStatus status = payPartners(transaction,
                    chargeInfo.getPartnerInfo().getAccount(),
                    chargeInfo.getPartnerInfo().getAmount());
            }
            transaction.setRevenueProcessed(true);

            //Settlement - check if should initiate after a payment
            if (TransactionType.PAYMENT.equals(transaction.getType())) {
                logger.debug("Settlement for:  TransactionId: {} {} userId: {}", transaction.getTransactionId(), transaction.getType(), transaction.getDestinations().get(0).getPayload().getAccount().getUserId());
                ResponseObject res = hitSettlementService(transaction.getDestinations().get(0).getPayload().getAccount(), transaction.getDestinations().get(0).getPayload().getTotal(), "/initiate");
            }

            logger.debug("Partner revenue recovered ... trigger commission {} {}",transaction.getId(),
                chargeInfo.getPartnerInfo().getAccount().getUserId());
            commissionService.pullCommission(transaction,this);

        } else {
            //Settlement
            if (TransactionType.SETTLEMENT.equals(transaction.getType())) {
                logger.debug("Settlement done:  TransactionId: {} {} userId: {}", transaction.getTransactionId(), transaction.getType(), transaction.getDestinations().get(0).getPayload().getAccount().getUserId());
                ResponseObject res = hitSettlementService(transaction.getDestinations().get(0).getPayload().getAccount(), transaction.getDestinations().get(0).getPayload().getTotal(), "/callback");
            }
            //Reversal
            if (TransactionType.REVERSAL.equals(transaction.getType()) && transaction.getPayload().getOrderInfo().getReference() != null) {
                logger.debug("REVERSAL done:  TransactionId: {} {} userId: {}", transaction.getTransactionId(), transaction.getType(), transaction.getDestinations().get(0).getPayload().getAccount().getUserId());
                ReversalOutgoingRequest request = hitReversalService(transaction);

            }
        }

    }

    public ResponseObject<PaymentSplitResponse> hitPaymentSplitService(
            net.tospay.transaction.entities.Transaction transaction) {

        try {
            logger.debug("hitSplitPaymentService {}", transaction.getId());

            PaymentRequest request = new PaymentRequest();
            // request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchantId(transaction.getPayload().getDelivery().get(0).getAccount().getUserId());
            request.setPaymentId(transaction.getPayload().getOrderInfo().getToken());
            request.setTransactionId(transaction.getTransactionId());
            request.setStatus(transaction.getTransactionStatus());

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentRequest>(request, headers);

            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject("splitPaymentUrl", entity, ResponseObject.class);
            logger.debug("hitPaymentPayService response {}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error("", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            description = description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    public ResponseObject<PaymentSplitResponse> hitPaymentPayService(
            net.tospay.transaction.entities.Transaction transaction) {

        try {

            logger.debug("hitPaymentPayService {} {} {}", transaction.getTransactionId(), transaction.getPayload().getOrderInfo().getReference(), transaction.getPayload().getOrderInfo().getToken());
            String url = paymentUrl;
            OrderType orderType = transaction.getPayload().getOrderInfo().getType();

            PaymentRequest request = new PaymentRequest();
            //request.setEmail(transaction.getPayload().getUserInfo().getEmail());
            request.setMerchantId(transaction.getPayload().getDelivery().get(0).getAccount().getUserId());
            request.setPaymentId(transaction.getPayload().getOrderInfo().getToken());
            request.setTransactionId(transaction.getTransactionId());
            request.setSenderId(transaction.getPayload().getUserInfo().getPhone());
            request.setStatus(transaction.getTransactionStatus());
            request.setAmount(transaction.getPayload().getOrderInfo().getAmount().getAmount());
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<PaymentRequest>(request, headers);

            logger.debug(" {}", request);
            ResponseObject<PaymentSplitResponse> response =
                    restTemplate.postForObject(url, entity, ResponseObject.class);
            logger.debug(" {} {} {}", response.getStatus(), response.getDescription(), response.getData());

            return response;
        } catch (HttpClientErrorException e) {
            logger.error("", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        } catch (Exception e) {
            logger.error("", e);
            String status = ResponseCode.FAILURE.type;
            String description = e.getMessage();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    public ResponseObject<StoreResponse> hitStore(AccountType accountType, TransferOutgoingRequest request) {
        try {
            String url = this.STORE_PAY_URLS.get(accountType.name());
            String callbackUrl = serverUrl;
            switch( accountType){
                case MOBILE:
                    callbackUrl += URL.API_VER+ URL.CALLBACK_MOBILE;
                    break;
                case BANK:
                    callbackUrl += URL.API_VER+ URL.CALLBACK_BANK;
                    break;
                case CARD:
                    callbackUrl += URL.API_VER+ URL.CALLBACK_CARD;
                    break;
            }
            request.setCallbackUrl(callbackUrl);

            this.logger.debug("hitStore request: {} {}", url, request);//.setHideData(false)

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(request, headers);

            ResponseEntity<ResponseObject<StoreResponse>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<ResponseObject<StoreResponse>>() {
            });
            this.logger.debug("hitStore response: {}", response);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("", e);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ResponseObject responseObject = objectMapper.readValue(e.getResponseBodyAsString(), ResponseObject.class);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(responseObject.getStatus(), responseObject.getDescription());
                errors.add(error);
                if (responseObject.getError() == null || responseObject.getError().isEmpty()) {
                    responseObject.setError(errors);
                }
                responseObject.setStatus(ResponseCode.FAILURE.type);
                return responseObject;
            } catch (JsonProcessingException j) {
                logger.error("", j);
                String status = ResponseCode.FAILURE.type;
                String description = j.getLocalizedMessage();
                description = description.substring(0, description.length() < 100 ? description.length() : 100);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);

                return new ResponseObject<>(status, description, errors, null);
            }


        } catch (Exception e) {
            logger.error("", e);

            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();//e.getLocalizedMessage();
       //     description = description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    public ResponseObject<StoreStatusResponse> hitStoreStatus(AccountType accountType, String storeReference) {
        try {
            if (storeReference == null) {
                this.logger.debug("hitStoreStatus null storeRef. failing the request {} {}", accountType);
                String status = ResponseCode.FAILURE.type;
                String description = ResponseCode.FAILURE.name();
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);
                return new ResponseObject<>(status, description, errors, null);
            }
            String url = this.STORE_STATUS_URLS.get(accountType.name());
            url = url.replace(":id", storeReference);
            this.logger.debug("hitStoreStatus request: {} {}", url);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransferOutgoingRequest>(headers);

            ResponseEntity<ResponseObject<StoreStatusResponse>> response = restTemplate
                .exchange(url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ResponseObject<StoreStatusResponse>>() {
                    });
            this.logger.debug("hitStoreStatus response: {}", response);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("", e);
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ResponseObject responseObject = objectMapper
                    .readValue(e.getResponseBodyAsString(), ResponseObject.class);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(responseObject.getStatus(),
                    responseObject.getDescription());
                errors.add(error);
                if (responseObject.getError() == null || responseObject.getError().isEmpty()) {
                    responseObject.setError(errors);
                }
                responseObject.setStatus(ResponseCode.FAILURE.type);
                return responseObject;
            } catch (JsonProcessingException j) {
                logger.error("", j);
                String status = ResponseCode.FAILURE.type;
                String description = j.getLocalizedMessage();
                description = description
                    .substring(0, description.length() < 100 ? description.length() : 100);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);

                return new ResponseObject<>(status, description, errors, null);
            }


        } catch (Exception e) {
            this.logger.error("", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getLocalizedMessage();
            description = description == null ? null
                : description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }

    //  @Transactional
    public CompletableFuture<Boolean> pushToDestination(final net.tospay.transaction.entities.Transaction transaction) {
        try {
            logger.debug("pushToDestination {}", transaction.getId());
            //if split bill enquire if sourcing complete
//            if (TransactionType.PAYMENT.equals(transaction.getPayload().getType())
//                    && OrderType.SPLIT.equals(transaction.getPayload().getOrderInfo().getType())) {
//                ResponseObject<PaymentSplitResponse> res = hitPaymentSplitService(transaction);
//
//                //if split complete pay merchant wallet
//                if (ResponseCode.SUCCESS.type.equalsIgnoreCase(res.getStatus()) && res.getData() != null
//                        && res.getData().isPay()) {
//                    PaymentSplitResponse response = res.getData();
//
//                    Transaction tran = new Transaction();
//                    tran.setPayload(null);//TODO payload null when internal request?
//                    tran.setTransactionStatus(TransactionStatus.CREATED);
//                    tran.setTransactionStatus(TransactionStatus.PROCESSING);
//                    Source sourceEntity = new Source();
//                    Store storeSource = new Store();
//                    storeSource.setAccount(response.getAccount());
//                    storeSource.setTotal(response.getAmount());
//                    sourceEntity.setPayload(storeSource);
//                    sourceEntity.setTransactionStatus(TransactionStatus.CREATED);
//                    tran.addSource(sourceEntity);
//                    sourceEntity.setTransaction(tran);
//
//                    Destination destinationEntity = new Destination();
//                    Store storeDest = new Store();
//                    storeDest.setAccount(response.getAccount());
//                    storeDest.setTotal(response.getAmount());
//                    destinationEntity.setPayload(storeDest);
//                    destinationEntity.setTransactionStatus(TransactionStatus.CREATED);
//                    tran.addDestination(destinationEntity);
//
//
//                    transactionRepository.saveAndFlush(tran);
//                    //trigger paying merchant async
//                    processDestinations(tran);
//                } else {
//                    logger.debug("split bill sourcing not complete yet {}", transaction.getId());
//                }
//            } else {

            processDestinations(transaction);
            // }
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    void processDestinations(final net.tospay.transaction.entities.Transaction transaction) {


        List<Destination> destinations = transaction
                .getDestinations();//transactionRepository.findById(transaction.getId()).get().getDestinations();
        destinations.forEach(destination -> {

            destination.setTransactionStatus(TransactionStatus.PROCESSING);

            TransferOutgoingRequest request = new TransferOutgoingRequest();
            request.setAccount((Account) Utils.deepCopy(destination.getPayload().getAccount()));
            request.setTransactionType(destination.getTransaction().getType());
            request.setMerchantReference(destination.getTransaction().getPayload().getOrderInfo().getReference());

            //hack remove later for moses  and his strict mode
            if (AccountType.WALLET.equals(destination.getPayload().getAccount().getType())) {
                request.getAccount().setCountry(null);
                request.setMerchantReference(null);
            }
            if (AccountType.MOBILE.equals(destination.getPayload().getAccount().getType())) {
                request.setMerchantReference(null);
            }
            if (Arrays.asList(AccountType.WALLET, AccountType.MOBILE)
                .contains(destination.getPayload().getAccount().getType())) {
                logger.debug("clear out fields thanks to mose strict mode :-( {}",
                    destination.getTransaction().getId());
                request.getAccount().setName(null);
                request.getAccount().setPhone(null);
                request.getAccount().setEmail(null);
                request.setTransactionType(null);
            }
            request.setAction("DESTINATION");
            request.setAmount(destination.getPayload().getTotal());
            request.setExternalReference(destination.getId());
            if(transaction.getSources().size()>0){ //reversal have no source sometimes?
            request.setFxId(transaction.getSources().get(0).getFxId());}
            request.setDescription(
                destination.getTransaction().getPayload().getOrderInfo().getDescription());
            destination.getRequest().put(LocalDateTime.now(), request);
            ResponseObject<StoreResponse> response = this
                .hitStore(destination.getPayload().getAccount().getType(), request);

            destination.getResponse().put(LocalDateTime.now(), response);
            destination
                .setStoreRef(response.getData() != null ? response.getData().getStoreRef() : null);
            destination.setAvailableBalance(
                response.getData() != null ? response.getData().getNewBalance() : null);
            destination.setFxId(response.getData() != null ? response.getData().getFxId() : null);
            if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {
                destination.setTransactionStatus(TransactionStatus.FAILED);
                if (response.getError() != null) {
                    destination.setCode(response.getError().get(0).getCode());
                    destination.setReason(response.getError().get(0).getDescription());
                }
            } else if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())) {
                destination.setTransactionStatus(TransactionStatus.SUCCESS);
                destination.setCode(ResponseCode.SUCCESS.type);
                destination.setReason(ResponseCode.SUCCESS.name());
            }

            logger.error("destination store hit  {} {}", destination.getId(), destination.getTransactionStatus());
            //java-util-concurrentmodificationexception for(int i = 0; i<myList.size(); i++){
            //destinationRepository.save(destination);
            // transactionRepository.save(transaction);
        });
        this.transactionRepository.saveAndFlush(
            transaction);// transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);
        processTransactionStatus(transaction, null);
    }


    //NB: External Reference here is UUID of Source or Destination.
    //NB: External Reference here is UUID of Source or Destination.
    public void processPaymentCallback(AsyncCallbackResponse response) {
        Transaction transaction = null;
        try {
            Optional<Source> optionalSource = response.getExternalReference() == null ? Optional.empty() : sourceRepository.findById(response.getExternalReference());
            Optional<net.tospay.transaction.entities.Destination> optionalDestination = response.getExternalReference() == null ? Optional.empty() : destinationRepository.findById(response.getExternalReference());


            if (!optionalDestination.isPresent() && !optionalSource.isPresent()) {
                logger.debug("TODO: Reference not found {}",response.getExternalReference());
                return;
            }
            if (response.getCode() == null) {
                logger.debug("TODO: response code must never be null {}", response.getCode());
            }


            BaseSource source = optionalSource.isPresent()?optionalSource.get():optionalDestination.get();

            transaction = source.getTransaction();
            source.setTransactionStatus(ResponseCode.SUCCESS.equals(response.getCode()) ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);

            source.setCode(response.getCode() != null ? response.getCode().type : null);
            source.setReason(response.getReason());
            source.setDescription(response.getDescription());
            source.setStoreRef(response.getStoreRef());
            source.getResponseAsync().put(LocalDateTime.now(), response);

            if(optionalSource.isPresent()){
                sourceRepository.save((Source)source);
            }else{
                destinationRepository.save((Destination) source);}

            if(optionalSource.isPresent() && ResponseCode.PROCESSING.equals(response.getCode())){// switch to pay bill?
                logger.debug("still processing .. paybill? {}", response.getCode());

                notifyService.notifyGateway(optionalSource.get(), StoreActionType.CREDIT,response.getInstructions());

            }else{
                processTransactionStatus(transaction, null);
            }

        } catch (Exception e) {
            logger.error("", e);
            processTransactionStatus(transaction, e);

        }
    }


    TransactionStatus payPartners(Transaction transaction, Account account, Amount amount) {

        this.logger.debug("revenue for transaction {} user {} amount {}", transaction.getId(),
            account.getUserId(), amount);

        Store store = new Store();
        store.setAccount(account);
        store.setCharge(new Amount(BigDecimal.ZERO, amount.getCurrency()));
        store.setOrder(amount);
        store.setTotal(amount);

        //A revenue has a new destination added to the transaction leg
        Destination destination = new Destination();
        destination.setTransaction(transaction);
        transaction.addDestination(destination);
        destination.setPayload(store);
        destination.setTransactionStatus(TransactionStatus.CREATED);
        destination.setRevenue(true);
        this.destinationRepository.save(destination);

        TransferOutgoingRequest request = new TransferOutgoingRequest();
        request.setAccount(store.getAccount());
        // request.setTransactionType(transaction.getType());
        //hack remove later for moses  and his strict mode
        if (AccountType.WALLET.equals(destination.getPayload().getAccount().getType())) {
            request.getAccount().setCountry(null);
        }
        if (Arrays.asList(AccountType.WALLET, AccountType.MOBILE).contains(destination.getPayload().getAccount().getType())) {
            logger.debug("clear out fields thanks to mose strict mode :-( {}", destination.getTransaction().getId());
            request.getAccount().setName(null);
            request.getAccount().setPhone(null);
            request.getAccount().setEmail(null);
        }
        request.setAction("DESTINATION");
        request.setAmount(store.getTotal());
        request.setExternalReference(destination.getId());
        if(transaction.getSources().size()>0){ //reversal have no source sometimes?
            request.setFxId(transaction.getSources().get(0).getFxId());}
        request.setDescription(
            "REVENUE: " + destination.getTransaction().getPayload().getOrderInfo()
                .getDescription());
        destination.getRequest().put(LocalDateTime.now(), request);

        ResponseObject<StoreResponse> response = this.hitStore(AccountType.WALLET, request);

        destination.getResponse().put(LocalDateTime.now(), response);
        destination
            .setStoreRef(response.getData() != null ? response.getData().getStoreRef() : null);
        destination.setAvailableBalance(
            response.getData() != null ? response.getData().getNewBalance() : null);
        destination.setFxId(response.getData() != null ? response.getData().getFxId() : null);
        if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {//failure
            destination.setTransactionStatus(TransactionStatus.FAILED);
            if (response.getError() != null) {
                destination.setCode(response.getError().get(0).getCode());
                destination.setReason(response.getError().get(0).getDescription());
            }
        } else if (ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())) {
            destination.setTransactionStatus(TransactionStatus.SUCCESS);
            destination.setCode(ResponseCode.SUCCESS.type);
            destination.setReason(ResponseCode.SUCCESS.name());
        }

        logger.error("destination store hit  {} {}", destination.getId(),
            destination.getTransactionStatus());
        this.destinationRepository.save(destination);
        //transactionRepository.saveAndFlush(transaction);

        return destination.getTransactionStatus();
    }

    ResponseObject hitSettlementService(Account account, Amount amount, String settlementType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            logger.debug("hitSettlementService {} {} {} {}", settlementType, account.getUserId(), account.getUserType(), amount);

            SettlementOutgoingRequest request = new SettlementOutgoingRequest();
            request.setAccount(account);
            request.setAmount(amount);

            HttpEntity entity = new HttpEntity<SettlementOutgoingRequest>(request, headers);

            ResponseObject<SettlementOutgoingRequest> response = restTemplate.postForObject(settlementUrl + settlementType, entity, ResponseObject.class);
            logger.debug("{}", response);
            if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {

                logger.debug("TODO: failure on getting settlement details {}", response);
            }
            return response;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    ReversalOutgoingRequest hitReversalService(Transaction transaction) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            this.logger.debug("hitReversalService {} {}", transaction.getPayload().getOrderInfo().getReference(), transaction.getPayload().getOrderInfo().getAmount().getAmount());

            ReversalOutgoingRequest request = new ReversalOutgoingRequest();
            request.setAmount(transaction.getPayload().getOrderInfo().getAmount());
            request.setPaymentId(transaction.getPayload().getOrderInfo().getReference());
            ResponseCode responseCode = TransactionStatus.SUCCESS.equals(transaction.getTransactionStatus()) ? ResponseCode.SUCCESS : ResponseCode.FAILURE;
            request.setStatus(responseCode);
            request.setId(transaction.getId());
            request.setReference(transaction.getPayload().getOrderInfo().getReference());

            HttpEntity entity = new HttpEntity<ReversalOutgoingRequest>(request, headers);

            ResponseObject<ReversalOutgoingRequest> response = restTemplate.postForObject(reversalCallbackUrl, entity, ResponseObject.class);
            logger.debug("{}", response);
            if (ResponseCode.FAILURE.type.equalsIgnoreCase(response.getStatus())) {

                logger.debug("TODO: failure on informing reversal service {}", response);
            }
            return response.getData();
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }


    public Transaction createTransaction(TransactionRequest request) {
        Transaction transaction = new Transaction();
        //  JsonNode node = mapper.valueToTree(request);

        transaction.setUserInfo(request.getUserInfo());
        transaction.setPayload(request);
        transaction.setTransactionStatus(
            request.getStatus() != null ? request.getStatus() : TransactionStatus.CREATED);
        transaction.setType(request.getType());

        String callbackUrl = null;
        if (transaction.getPayload().getChargeInfo().getPartnerInfo() != null
            && request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl() != null) {
            logger.debug("hack: partner transaction. set account callbackurl {} {}",
                request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl());
            callbackUrl = request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl();
        }

        for (int x = 0; x < request.getSource().size(); x++) {
            Store topupValue = request.getSource().get(x);
            if (callbackUrl != null) {
                topupValue.getAccount().setCallbackUrl(callbackUrl);
            }
            if (topupValue.getAccount().getEmail() == null) {//null source details -fetch from user info
                logger.debug("hack: null source details - fetch from user info userId {} ",
                    topupValue.getAccount().getUserId());
                topupValue.getAccount().setEmail(transaction.getUserInfo().getEmail());
                if (topupValue.getAccount().getName() == null) {
                    topupValue.getAccount().setName(transaction.getUserInfo().getName());
                }
                if (topupValue.getAccount().getPhone() == null) {
                    topupValue.getAccount().setPhone(transaction.getUserInfo().getPhone());
                }
                if (topupValue.getAccount().getCountry() == null) {
                    topupValue.getAccount().setCountry(transaction.getUserInfo().getCountry());
                }
            }
            if (transaction.getPayload().getChargeInfo().getPartnerInfo() != null
                && request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl() != null) {
                logger.debug("hack: partner transaction. set account callbackurl {} {}",
                    topupValue.getAccount().getUserId(),
                    request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl());
                topupValue.getAccount()
                    .setCallbackUrl(request.getChargeInfo().getPartnerInfo().getAccount().getCallbackUrl());
            }

            Source sourceEntity = new Source();
            // sourceEntity.setTransaction(transaction);
            sourceEntity.setPayload(topupValue);
            sourceEntity.setTransactionStatus(TransactionStatus.CREATED);
            sourceEntity.setTransaction(transaction);
            transaction.addSource(sourceEntity);
        }

        for (int x = 0; x < request.getDelivery().size(); x++) {

            Store topupValue = request.getDelivery().get(x);
            if (callbackUrl != null) {
                topupValue.getAccount().setCallbackUrl(callbackUrl);
            }

            Destination destinationEntity = new Destination();
            // destinationEntity.setTransaction(transaction);
            destinationEntity.setPayload(topupValue);
            destinationEntity.setTransactionStatus(TransactionStatus.CREATED);
            destinationEntity.setTransaction(transaction);

            transaction.addDestination(destinationEntity);

        }

        transactionRepository.save(transaction);
        return transaction;
    }


}
