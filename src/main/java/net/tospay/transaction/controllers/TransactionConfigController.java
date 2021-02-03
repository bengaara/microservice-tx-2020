package net.tospay.transaction.controllers;

import java.util.Arrays;
import java.util.Optional;
import net.tospay.transaction.entities.TransactionConfig;
import net.tospay.transaction.enums.MakerCheckerStatus;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.TransactionConfigRequest;
import net.tospay.transaction.models.TransactionInitRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.ReversalRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionConfigRepository;
import net.tospay.transaction.repositories.TransactionInitRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class TransactionConfigController extends BaseController {

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
    TransactionInitRepository transactionRequestRepository;

    @Autowired
    TransactionConfigRepository transactionConfigRepository;

    public TransactionConfigController(FundService fundService, CrudService crudService) {
        this.fundService = fundService;
        this.crudService = crudService;

    }

    @PostMapping(URL.TRANSACTION_CONFIG_MAKE)
    public ResponseObject<BaseModel> transactionMake(@RequestBody TransactionConfigRequest request) {

        TransactionConfig transactionConfig = transactionConfigRepository.findLatestConfig().get();

        if (MakerCheckerStatus.PENDING.equals(transactionConfig.getMCStatus())) {

            String status = ResponseCode.CHANGES_ALREADY_QUEUED.type;
            String description = ResponseCode.CHANGES_ALREADY_QUEUED.name();
            logger.info("{} {} {} reversalparent {} ", description, transactionConfig.getId());
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), null));
        }

        TransactionConfig model = (TransactionConfig) Utils.deepCopy(transactionConfig);

        if (request.getAirtimeLimit() != null) {
            model.setAirtimeLimit(request.getAirtimeLimit());
        }
        if (request.getTransactionApprovalCount() != null) {
            model.setTransactionApprovalCount(request.getTransactionApprovalCount());
        }
        if (request.getReversalApprovalCount() != null) {
            model.setReversalApprovalCount(request.getReversalApprovalCount());
        }
        if (request.getDailyTransferLimit() != null) {
            model.setDailyTransferLimit(request.getDailyTransferLimit());
        }
        if (request.getEvalueApprovalCount() != null) {
            model.setEvalueApprovalCount(request.getEvalueApprovalCount());
        }
        transactionConfig.setMaker(request.getUserInfo());
        transactionConfig.setMCStatus(MakerCheckerStatus.PENDING);
        transactionConfig.getCheckerRecords().clear();
        transactionConfig.setRecord(model);

        transactionConfigRepository.save(transactionConfig);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            transactionConfig.getMCStatus());//return mapResponse(response);

    }

    @PostMapping(Constants.URL.TRANSACTION_CONFIG_CHECK)
    public ResponseObject<BaseModel> transactionCheck(@RequestBody TransactionInitRequest request) {

        if (request.getAction()
            == null) {//|| Arrays.asList(MakerCheckerStatus.APPROVED,MakerCheckerStatus.PENDING).contains(request.getAction())) {// || MakerCheckerStatus. request.getAction()
            String status = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED.type;
            String description = ResponseCode.APPROVAL_STATUS_NOT_ALLOWED
                .name();// ResponseCode.FAILURE.name();
            logger.info("{} {}", description, request);
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description,
                Arrays.asList(new Error(status, description)), request));
        }
        Optional<TransactionConfig> optional = transactionConfigRepository.findLatestConfig();

//        if (!optional.isPresent()) {
//            String status = ResponseCode.TRANSACTION_REQUEST_NOT_FOUND.type;
//            String description = ResponseCode.TRANSACTION_REQUEST_NOT_FOUND.name();
//            logger.info("{} {}", description, request);
//            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type, description, Arrays.asList(new Error(status, description)), request));
//        }
        //update pending only
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
            .stream().anyMatch(configRequest ->
            {
                boolean exists = configRequest.getUserInfo().getUserId()
                    .equals(request.getUserInfo().getUserId());
                if (exists) {
                    logger.info("user already checked. Skipping his attempt {} {}",
                        request.getUserInfo().getUserId());
                }
                return exists;
            })) {
            optional.get().addCheckerRecord(request);
        }

        //process
        switch (request.getAction()) {
            case APPROVED:

                if (optional.get().getApprovalCount()
                    .equals(optional.get().getCheckerRecords().size())) {
                    logger
                        .info("getApprovalCount reached {} {}", optional.get().getApprovalCount());

                    TransactionConfig model = (TransactionConfig) Utils
                        .deepCopy(optional.get().getRecord());
                    model.setId(optional.get().getId());
                    model.setMCStatus(MakerCheckerStatus.APPROVED);
                    model.setCheckerRecords(optional.get().getCheckerRecords());
                    model.setMaker(optional.get().getMaker());

                    transactionConfigRepository.save(model);

                }
                break;
//            case PENDING:
//                optional.get().getChecker().clear();
//                optional.get().setMCStatus(MakerCheckerStatus.PENDING);
//                break;
//            case VOID:
//                optional.get().getChecker().clear();
//                optional.get().setMCStatus(MakerCheckerStatus.VOID);
//                break;
            case REJECT:
                optional.get().setMCStatus(MakerCheckerStatus.REJECT);
                break;
        }


        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            optional.get().getMCStatus());//return mapResponse(response);
    }

    @PostMapping(URL.TRANSACTION_CONFIG_FETCH)
    public ResponseObject<BaseModel> transactionfetch(
        @RequestBody TransactionConfigRequest request) {

        TransactionConfig transactionConfig = transactionConfigRepository.findLatestConfig().get();

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            transactionConfig);//return mapResponse(response);

    }
}
