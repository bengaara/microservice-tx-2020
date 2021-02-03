package net.tospay.transaction.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.entities.TransactionConfig;
import net.tospay.transaction.entities.TransactionInit;
import net.tospay.transaction.enums.AccountType.AccountSubType;
import net.tospay.transaction.enums.FraudStatus;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.TransactionInitRequest;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.models.response.TransactionInitFetchResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.ReversalRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionConfigRepository;
import net.tospay.transaction.repositories.TransactionInitRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.DashboardService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.TransactionValidateService;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class TransactionInitController extends BaseController {

    CrudService crudService;
    FundService fundService;
    TransactionValidateService transactionValidateService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    DestinationRepository destinationRepository;

    @Autowired
    ReversalRepository reversalRepository;

    @Autowired
    TransactionInitRepository transactionInitRepository;

    @Autowired
    TransactionConfigRepository transactionConfigRepository;

    @Autowired
    DashboardService dashboardService;

    public TransactionInitController(FundService fundService, CrudService crudService,
        DashboardService dashboardService,TransactionValidateService transactionValidateService) {
        this.fundService = fundService;
        this.crudService = crudService;
        this.dashboardService = dashboardService;
        this.transactionValidateService =transactionValidateService;

    }

    @PostMapping(URL.TRANSACTION_INIT_MAKE)
    public ResponseObject<BaseModel> transactionMake(@RequestBody TransactionRequest request) {

        List<Error> errors = transactionValidateService.checkValidityErrors(request);
        if (!errors.isEmpty()) {
            throw new ResponseErrorObject(
                new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    errors, null));
        }

        TransactionConfig transactionConfig = transactionConfigRepository.findLatestConfig().get();
        TransactionInit transactionRequest = new TransactionInit();

        Integer approvalCount = transactionConfig.getTransactionApprovalCount();

        //if initial e-value - 3 checkers
        if (AccountSubType.GLOBAL_TOTAL_VALUE
            .equals(request.getDelivery().get(0).getAccount().getSubType())) {
            approvalCount = transactionConfig.getEvalueApprovalCount();
        }

        transactionRequest.setPayload(request);
        transactionRequest.setUserInfo(request.getUserInfo());
        transactionRequest.setApprovalCount(approvalCount);
        transactionRequest.setMaker(request.getUserInfo());

        transactionRequest.setMCStatus(MakerCheckerStatus.PENDING);

        //  transactionRequest.setFraudInfo(transactionRequest.getFraudInfo());

        transactionInitRepository.save(transactionRequest);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            transactionRequest.getId());//return mapResponse(response);
    }

    @PostMapping(URL.TRANSACTION_INIT_CHECK)
    public ResponseObject<BaseModel> transactionCheck(@RequestBody TransactionInitRequest request) {

        if (request.getFraudInfo() != null && !FraudStatus.PROCEED
            .equals(request.getFraudInfo().getStatus())) {
            //check if fraud marked it as dont proceed
            return new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                null, null);
        }

        if (request.getAction()
            == null) {//|| Arrays.asList(MakerCheckerStatus.APPROVED,MakerCheckerStatus.PENDING).contains(request.getAction())) {// || MakerCheckerStatus. request.getAction()
            String status = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED.type;
            String description = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED
                .name();// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }

        Optional<TransactionInit> optional = transactionInitRepository.findById(request.getId());
        if (!optional.isPresent()) {

            optional = transactionInitRepository
                .findByFraudReference(request.getFraudInfo().getFraudQuery());
            if (!optional.isPresent()) {

                String status = ResponseCode.TRANSACTION_REQUEST_NOT_FOUND.type;
                String description = ResponseCode.TRANSACTION_REQUEST_NOT_FOUND.name();
                logger.info("{} {}", description, request);
                throw new ResponseErrorObject(
                    new ResponseObject(ResponseCode.FAILURE.type, description,
                        Arrays.asList(new Error(status, description)), null));
            }
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

        if (optional.get().getCheckerRecords() == null || !optional.get().getCheckerRecords()
            .stream().anyMatch(initRequest ->
            {
                boolean exists = initRequest.getUserInfo().getUserId()
                    .equals(request.getUserInfo().getUserId());
                if (exists) {
                    logger.info("user already checked. Skipping his attempt {} {}",
                        request.getUserInfo().getUserId());
                }
                return exists;
            })) {
            optional.get().addCheckerRecord(request);
            optional.get().setFraudInfo(request.getFraudInfo());
        }

        transactionInitRepository.save(optional.get());

        TransactionInit model = optional.get();

        //process
        switch (request.getAction()) {
            case APPROVED:

                if (model.getApprovalCount().equals(model.getCheckerRecords().size())) {
                    logger.info("getApprovalCount reached {} {}", model.getApprovalCount());

                    model.setMCStatus(MakerCheckerStatus.APPROVED);
                    Transaction transaction = fundService.createTransaction(model.getPayload());
                    CompletableFuture<Pair<Transaction, List<String>>> future = fundService
                        .pullFromSource(transaction);
                }
                break;
//            case PENDING:
//                model.getChecker().clear();
//                model.setMCStatus(MakerCheckerStatus.PENDING);
//                break;
//            case VOID:
//                model.getChecker().clear();
//                model.setMCStatus(MakerCheckerStatus.VOID);
//                break;
            case REJECT:
                //model.getCheckerRecords().clear();
                model.setMCStatus(MakerCheckerStatus.REJECT);
                break;
        }

        //  model.setMCStatus(request.getAction());

        transactionInitRepository.save(model);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            model.getMCStatus());//return mapResponse(response);

    }

    @PostMapping(URL.TRANSACTION_INIT_FETCH)
    public ResponseObject<BaseModel> transactionfetch(
        @RequestBody TransactionInitRequest request) {

        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }
        UUID agentId = request.getUserInfo().getUserId();
        if (Arrays.asList(UserType.AGENT, UserType.MERCHANT)
            .contains(request.getUserInfo().getTypeId())) {
            agentId = request.getUserInfo().getAgentId();
        } else if (Arrays.asList(UserType.ADMIN).contains(request.getUserInfo()
            .getTypeId())) {//fetch system accounts data - they have no agentid i hope?
            agentId = null;
        }

        MakerCheckerStatus status =
            request.getAction() != null ? request.getAction() : MakerCheckerStatus.PENDING;
        Integer offset = request.getOffset() != null ? request.getOffset() : 0;

        LocalDate f = request.getFrom() == null ? LocalDateTime.now().minusYears(1).toLocalDate()
            : request.getFrom();
        LocalDate t = request.getTo() == null ? LocalDateTime.now().toLocalDate().plusDays(1)
            : request.getTo();//.atStartOfDay();

        List<TransactionInit> list = dashboardService.fetchTransactionInit(agentId, status,
            request.getCheckerStage(),
            offset,
            limit, f, t);

        List<TransactionInitFetchResponse> list1 = list.stream().map(transactionInit -> {

            return TransactionInitFetchResponse.from(transactionInit);

        }).collect(Collectors.toList());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list1);

    }
}
