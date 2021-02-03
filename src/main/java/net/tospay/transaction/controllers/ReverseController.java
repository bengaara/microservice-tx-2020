package net.tospay.transaction.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.transaction.Transactional;
import net.tospay.transaction.entities.Reversal;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.entities.TransactionConfig;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.ReverseRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.ReversalRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionConfigRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.DashboardService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.ReversalService;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class ReverseController extends BaseController {

    CrudService crudService;
    FundService fundService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DestinationRepository destinationRepository;

    @Autowired
    ReversalRepository reversalRepository;

    @Autowired
    TransactionConfigRepository transactionConfigRepository;

    DashboardService dashboardService;

    ReversalService reversalService;

    public ReverseController(FundService fundService, CrudService crudService,
        DashboardService dashboardService, ReversalService reversalService) {
        this.fundService = fundService;
        this.crudService = crudService;
        this.dashboardService = dashboardService;
        this.reversalService = reversalService;

    }

    //Agent and admin - for agent theirs only?
    @PostMapping(Constants.URL.REVERSAl_MAKE)

    public ResponseObject<BaseModel> reversalMake(@RequestBody ReverseRequest request) {
        Optional<Transaction> optional = Optional.empty();

        if (request.getId() != null) {
            optional = crudService.fetchTransactionById(request.getId());

        } else if (request.getTransactionId() != null) {
            optional = crudService.fetchTransactionByTransactionId(request.getTransactionId());

        } else if (request.getPaymentId() != null) {

            optional = transactionRepository.findByPaymentId(request.getPaymentId());
        }

        if (!optional.isPresent()
            || !Arrays.asList(TransactionStatus.SUCCESS, TransactionStatus.FAILED)
            .contains(optional.get().getTransactionStatus())) {//only success/failed tx
            String status = ResponseCode.TRANSACTION_NOT_FOUND.type;
            String description = "no transaction found";// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }
        //only if partial success happened
        if (!optional.get().getSources().stream().filter(s -> {
            return (s.getTransactionStatus().equals(TransactionStatus.SUCCESS));
        }).findFirst().isPresent()) {
            String status = ResponseCode.REVERSAL_NOT_AUTHORISED_TRANSACTION_FAILED.type;
            String description = "REVERSAL_NOT_AUTHORISED_TRANSACTION_FAILED";// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
            
        }

        if (TransactionType.REVERSAL.equals(optional.get().getType()) || optional.get().getReversalChild() != null)
        {
            String status = ResponseCode.REVERSAL_ALREADY_QUEUED.type;
            String description = "REVERSAL_ALREADY_QUEUED";// ResponseCode.FAILURE.name();
            logger.info("{} {} {} reversalparent {} ", description, optional.get().getId(),
                optional.get().getType(), optional.get().getReversalParent());
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }

        Optional<Reversal> optionReversal = reversalRepository.findById(optional.get().getId());
        if (optionReversal.isPresent() &&  !MakerCheckerStatus.REJECT.equals(optionReversal.get().getMCStatus())) {

            String status = ResponseCode.REVERSAL_ALREADY_QUEUED.type;
            String description = "REVERSAL_ALREADY_QUEUED";// ResponseCode.FAILURE.name();
            logger.info("{} {} {} reversalparent {} ", description, optional.get().getId(),
                optional.get().getType(), optional.get().getReversalParent());
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }

        if (request.getAmount() != null && request.getAmount()
            .compareTo(optional.get().getPayload().getOrderInfo().getAmount().getAmount()) > 0) {
            String status = ResponseCode.REVERSAL_AMOUNT_EXCEEDED.type;
            String description = ResponseCode.REVERSAL_AMOUNT_EXCEEDED
                .name();// ResponseCode.FAILURE.name();
            logger.info("{} {}  {} {} ", description, optional.get().getId(), request.getAmount(),
                optional.get().getType(),
                optional.get().getPayload().getOrderInfo().getAmount().getAmount());
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)),null));

        }

        TransactionConfig transactionConfig = transactionConfigRepository.findLatestConfig().get();

        Reversal reversal = optionReversal.isPresent()?optionReversal.get(): new Reversal();
        reversal.setId(optional.get().getId());
        reversal.setPayload(request);
        reversal.setReverseCharge(request.getReverseCharge());
        reversal.setAmount(request.getAmount());
        reversal.setApprovalCount(transactionConfig.getReversalApprovalCount());
        reversal.setMaker(request.getUserInfo());
        reversal.setMCStatus(MakerCheckerStatus.PENDING);
    //    reversal.setTransaction(optional.get());
        reversal.getCheckerRecords().clear();
      //  reversal.setTransactionId(optional.get().getTransactionId());
        //  optional.get().setReversal(reversal);
        reversal.setReason(request.getReason());
       // reversal.setRecord(request);

        reversalRepository.save(reversal);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            reversal.getId());//return mapResponse(response);

    }

    @Transactional
    @PostMapping(Constants.URL.REVERSAl_CHECK)
    public ResponseObject<BaseModel> reversalCheck(@RequestBody ReverseRequest request) {

        if (request.getAction() == null) {//|| Arrays.asList(MakerCheckerStatus.APPROVED,MakerCheckerStatus.PENDING).contains(request.getAction())) {// || MakerCheckerStatus. request.getAction()
            String status = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED.type;
            String description = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED
                .name();// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), request));
        }

        Optional<Reversal> optional = Optional.empty();

        if (request.getReverseId() != null) {
            optional = reversalRepository.findById(request.getReverseId());

        } else if (request.getId() != null) {
            optional = reversalRepository.findById(request.getId());

        } else if (request.getTransactionId() != null) {

            optional = reversalRepository.findByTransactionId(request.getTransactionId());
        }

        if (!optional.isPresent()) {
            String status = ResponseCode.REVERSAL_NOT_FOUND.type;
            String description = ResponseCode.REVERSAL_NOT_FOUND.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), request));
        }
        if (!MakerCheckerStatus.PENDING.equals(optional.get().getMCStatus())) {
            String status = ResponseCode.APPROVAL_STATUS_NOT_UPDATABLE.type;
            String description = ResponseCode.APPROVAL_STATUS_NOT_UPDATABLE
                .name();// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }

        //maker cant be checker
        if (optional.get().getMaker().getUserId().equals(request.getUserInfo().getUserId())) {
            String status = ResponseCode.MAKER_CANT_BE_CHECKER.type;
            String description = ResponseCode.MAKER_CANT_BE_CHECKER.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description, Arrays.asList(new Error(status, description)), null));
        }

        Reversal reversal = optional.get();
        // reversal.setMCStatus(request.getAction());
       // reversal.setRecord(request);
        if (reversal.getCheckerRecords()==null || !reversal.getCheckerRecords().stream().anyMatch(reverseRequest ->
        {
            boolean exists = reverseRequest.getUserInfo().getUserId().equals(request.getUserInfo().getUserId());
            if(exists){
                logger.info("user already checked. Skipping his attempt {} {}", request.getUserInfo().getUserId());
            }
            return exists;

        })) {
            reversal.addCheckerRecord(request);
        }

        reversalRepository.save(optional.get());

        //process
        switch (request.getAction()) {
            case APPROVED:

                if (reversal.getApprovalCount() == reversal.getCheckerRecords().size()) {
                    logger.info("getApprovalCount reached {} {}", reversal.getApprovalCount());
                    reversal.setMCStatus(MakerCheckerStatus.APPROVED);
                    Optional<Transaction> opt = transactionRepository.findById(reversal.getId());
                    Transaction reversalTransaction = reversalService.reverseTransaction(opt.get(), reversal);
                    if(reversalTransaction !=null) {
                        //trigger sourcing async
                        CompletableFuture<Pair<Transaction, List<String>>> future = fundService
                            .pullFromSource(reversalTransaction);
                    }

                }
                break;
//            case PENDING:
//                reversal.getChecker().clear();
//                reversal.setMCStatus(MakerCheckerStatus.PENDING);
//                break;
//            case VOID:
//                reversal.getChecker().clear();
//                reversal.setMCStatus(MakerCheckerStatus.VOID);
//                break;
            case REJECT:
               // reversal.getCheckerRecords().clear();
                reversal.setMCStatus(MakerCheckerStatus.REJECT);
                break;
        }

        reversalRepository.save(reversal);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            reversal.getMCStatus());//return mapResponse(response);

    }

    @PostMapping(URL.REVERSAl_FETCH)
    public ResponseObject<BaseModel> reversalfetch(
        @RequestBody ReverseRequest request) {

        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        List<Reversal> list = dashboardService
            .fetchReversal(request.getAction(), request.getCheckerStage(),
                request.getOffset(),
                limit, request.getFrom(), request.getTo());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);

    }
}
