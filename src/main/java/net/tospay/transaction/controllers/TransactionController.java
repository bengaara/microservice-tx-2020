package net.tospay.transaction.controllers;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.validation.Valid;
import net.tospay.transaction.configs.JobSchedule;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.FraudStatus;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.AsyncCallbackResponse;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.FraudInfo;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.request.TransactionCommissionRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.models.response.TransferResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.TransactionValidateService;
import net.tospay.transaction.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class TransactionController extends BaseController {

    CrudService crudService;
    FundService fundService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DestinationRepository destinationRepository;

    TransactionValidateService transactionValidateService;

 //   @Autowired
 //  JaegerTracer tracer;


    public TransactionController(FundService fundService, CrudService crudService,
        TransactionValidateService transactionValidateService) {
        this.fundService = fundService;
        this.crudService = crudService;
        this.transactionValidateService =transactionValidateService;

    }

    @PostMapping(Constants.URL.TRANSFER)
    public ResponseObject<BaseModel> transfer(
            @Valid @RequestBody TransactionRequest request) {//Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
        // TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
        //  logger.info(" {}", request);

        return process(request);

    }

    @PostMapping(Constants.URL.TRANSFER_STAGE)
    public ResponseObject<BaseModel> transferStage(
            @Valid @RequestBody TransactionRequest request) {//Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
        // TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
        //  logger.info(" {}", request);

        return process(request);
    }

    @PostMapping(Constants.URL.TRANSFER_CONTINUE)
    public ResponseObject<BaseModel> transferContinue(
            @Valid @RequestBody FraudInfo fraudInfo) {//Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
        // TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
        //  logger.info(" {}", request);

        Optional<Transaction> optional = transactionRepository.findByFraudReference(fraudInfo.getFraudQuery());
        if (!optional.isPresent()) {
            String msg ="transferContinue: no transaction found";
            logger.debug(" {} {}",msg, fraudInfo.getFraudQuery());
            String status = ResponseCode.FAILURE.type;
            String description = msg;
            throw new ResponseErrorObject(new ResponseObject(status, description, Arrays.asList(new Error(status, description)),
                    null));
        }
        if (optional.get().getFraudInfo() !=null) {
            String msg ="transaction already unstaged";
            logger.debug("{}  {}",msg, optional.get().getId());
            String status = ResponseCode.FAILURE.type;
            String description = msg;
            throw new ResponseErrorObject(new ResponseObject(status, description, Arrays.asList(new Error(status, description)),
                null));
        }


        optional.get().setFraudInfo(fraudInfo);
        transactionRepository.save(optional.get());
        if (FraudStatus.PROCEED.equals(fraudInfo.getStatus())) {
            return triggerSource(optional.get());
        } else {
            //check if fraud marked it as dont proceed
            fundService.processTransactionStatus(optional.get(),null);

            return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(),
                null, null);
        }

    }

    @PostMapping(Constants.URL.TRANSFER_LOG)
    public ResponseObject<BaseModel> logTransaction(TransactionRequest request) {

        List<Error> errors = transactionValidateService.checkValidityErrors(request);
        if (!errors.isEmpty()) {
            throw new ResponseErrorObject(
                new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    errors, null));
        }
        Transaction transaction = fundService.createTransaction(request);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(transaction.getTransactionStatus());
        transferResponse.setTransactionId(transaction.getTransactionId());
        transferResponse.setId(transaction.getId());
        transferResponse.setReference(transaction.getPayload().getOrderInfo().getReference());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            transferResponse);//return mapResponse(response);
    }


    public ResponseObject<BaseModel> process(TransactionRequest request) {
        //JaegerSpan span = tracer.buildSpan("transfer").start();
        List<Error> errors = transactionValidateService.checkValidityErrors(request);
        if (!errors.isEmpty()) {
            throw new ResponseErrorObject(
                new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    errors, null));
        }

        JobSchedule.setEnableCheckAyncTransactionStatusCheck(true);

        Transaction transaction = fundService.createTransaction(request);

        //if fraud data exists //theres a card switch to sync?
        if (transaction.getPayload().getFraudInfo() != null &&
                (FraudStatus.SHOW_3DES.equals(transaction.getPayload().getFraudInfo().getStatus()) || FraudStatus.PROCEED.equals(transaction.getPayload().getFraudInfo().getStatus()))) {

         //   span.finish();
            return triggerSource(transaction);
        } else {

            //check if fraud marked it as dont proceed
            fundService.processTransactionStatus(transaction,null);

            TransferResponse transferResponse = new TransferResponse();
            transferResponse.setStatus(transaction.getTransactionStatus());
            transferResponse.setTransactionId(transaction.getTransactionId());
            transferResponse.setId(transaction.getId());
            transferResponse.setReference(transaction.getPayload().getOrderInfo().getReference());
    //        span.finish();
            return new ResponseObject(ResponseCode.PROCESSING.type, ResponseCode.PROCESSING.name(), null,
                    transferResponse);//return mapResponse(response);
        }
    }


    private ResponseObject<BaseModel> triggerSource(Transaction transaction) {

        logger.debug("triggerSource {}", transaction.getId());
        //trigger sourcing async
        CompletableFuture<Pair<Transaction, List<String>>> future = fundService.pullFromSource(transaction);

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setStatus(transaction.getTransactionStatus());
        transferResponse.setTransactionId(transaction.getTransactionId());
        transferResponse.setId(transaction.getId());
        transferResponse.setReference(transaction.getPayload().getOrderInfo().getReference());
        //if one card has 3dsecure, return html block and make sync
        List<Source> list = transaction.getSources().stream().filter(source -> {
            return AccountType.CARD.equals(source.getPayload().getAccount().getType());
        }).collect(Collectors.toList());
        //make syncronous and extract result
        if (!list.isEmpty()) {
            logger.debug("found card transaction.. switching to sync mode {} {}", transaction.getId(), list.get(0).getId());
            Pair<Transaction, List<String>> results = null;
            try {
                results = future.get(20, TimeUnit.SECONDS);
                transaction = results.getFirst();//transactionRepository.findById(transaction.getId()).get();//refetch
                transferResponse.setStatus(transaction.getTransactionStatus());
                if (!results.getSecond().isEmpty()) {
                    logger.debug("found 3D secure {}", transaction.getId());
                    transferResponse.setHtml(results.getSecond().get(0));
                }
                transferResponse.setTransactionId(transaction.getTransactionId());
                transferResponse.setId(transaction.getId());
            } catch (Exception e) {
                logger.error(transaction.getId().toString(), e);
            }

        }

        ResponseCode responseCode = TransactionStatus.FAILED.equals(transaction.getTransactionStatus()) ? ResponseCode.FAILURE : ResponseCode.PROCESSING;
        Error error = new Error(transaction.getCode(), transaction.getReason());


        return new ResponseObject(responseCode.type, responseCode.name(), ResponseCode.FAILURE.equals(responseCode) ? Arrays.asList(error) : null,
                ResponseCode.FAILURE.equals(responseCode) ? null : transferResponse);//return mapResponse(response);

    }

    @PostMapping({Constants.URL.CALLBACK_CARD
            , Constants.URL.CALLBACK_MOBILE
            , Constants.URL.CALLBACK_BANK})
    public ResponseObject<BaseModel> processStoreCallback(@RequestBody AsyncCallbackResponse response) {

        Optional<Source> optionalSource = response.getExternalReference() == null ? Optional.empty() :
                sourceRepository.findById(response.getExternalReference());
        Optional<net.tospay.transaction.entities.Destination> optionalDestination =
                response.getExternalReference() == null ? Optional.empty() :
                        destinationRepository.findById(response.getExternalReference());
        Transaction t = optionalSource.isPresent()?optionalSource.get().getTransaction():
            optionalDestination.isPresent()?optionalDestination.get().getTransaction():null;
        if (!optionalSource.isPresent() && !optionalDestination.isPresent()) {
            logger.error("no transaction found {}", response.getExternalReference());
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();


            return new ResponseObject(status, description, Arrays.asList(new Error(status, description)),
                    "no transaction found");
        }

        if (Arrays.asList(TransactionStatus.FAILED,TransactionStatus.SUCCESS).contains(t.getTransactionStatus())) {
            logger.error("transaction complete cant edit. {} tid- {}", response.getExternalReference(),t.getId());
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();

            return new ResponseObject(status, description, Arrays.asList(new Error(status, description)),
                "transaction complete cant edit");
        }
        fundService.processPaymentCallback(response);

        String status = ResponseCode.SUCCESS.type;
        String description = ResponseCode.SUCCESS.name();
        return new ResponseObject(status, description, null, response.getExternalReference());
    }

    @PostMapping(Constants.URL.TRANSFER_COMMISSION)
    public ResponseObject<List<TransactionFetchResponse>> transferCommission(@Valid @RequestBody TransactionCommissionRequest request) {
        // logger.info(" {} ", request);

        List<Transaction> list = null;

        if (request.calculateForOne()) {
            logger.info("transferCommission for  {} ", request);
            Optional<Transaction> optional = Optional.empty();

            if (request.getId() != null || request.getTransactionId() != null) {
                optional = crudService.fetchTransactionById(request.getId());
            } else if (request.getTransactionId() != null) {
                optional = crudService.fetchTransactionByTransactionId(request.getTransactionId());
            }

            if (!optional.isPresent()) {
                String status = ResponseCode.TRANSACTION_NOT_FOUND.type;
                String description = "no transaction found";// ResponseCode.FAILURE.name();
                logger.info("{} {}", description, request);
                throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description, Arrays.asList(new Error(status, description)), request));
            }

            if (request.getUserId() != null) {
                logger.info("transferCommission for user {} ", request.getUserId());
                list = crudService.fetchTransactionByPartnerIdAndUserId(null, request.getUserId(), 0, 10000);
            }


        }

        if (list == null || list.isEmpty()) {
            String status = ResponseCode.TRANSACTION_NOT_FOUND.type;
            String description = "no transaction found";// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description, Arrays.asList(new Error(status, description)), request));
        }

        logger.info("transferCommission for all pending users {} ");


        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, null);
    }

}
