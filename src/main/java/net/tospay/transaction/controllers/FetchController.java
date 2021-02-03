package net.tospay.transaction.controllers;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.Valid;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.request.TransactionFetchRequest;
import net.tospay.transaction.models.request.TransactionFindRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.StatementResponse;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.DashboardService;
import net.tospay.transaction.services.EmailService;
import net.tospay.transaction.services.SMSService;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import net.tospay.transaction.util.Utils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class FetchController extends BaseController {

    CrudService crudServiced;

    DashboardService dashboardService;

    SMSService SMSService;
    EmailService emailService;
    TransactionRepository transactionRepository;


    public FetchController(CrudService crudServiced, DashboardService dashboardService,
        SMSService SMSService, EmailService emailService,
        TransactionRepository transactionRepository) {
        this.crudServiced = crudServiced;
        this.dashboardService = dashboardService;
        this.SMSService = SMSService;
        this.emailService = emailService;
        this.transactionRepository = transactionRepository;
    }

    //used by user,and merchant
    @PostMapping(Constants.URL.TRANSACTIONS_FETCH)
    public ResponseObject<List<TransactionFetchResponse>> fetch(
        @Valid @RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);

        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }

        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        String userId = request.getUserInfo().getUserId().toString();
        if (request.getUserInfo().getAgentId() != null) {
            userId = request.getUserInfo().getAgentId().toString();
        }

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(request.getUserInfo().getTypeId(), userId
                , request.getOffset(),
                limit, request.getFrom(), request.getTo());
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);
    }
    @PostMapping(URL.TRANSACTIONS_FETCH_LIKE)
    public ResponseObject<List<TransactionFetchResponse>> fetchLike(
        @Valid @RequestBody TransactionFindRequest request) {
        // logger.info(" {} ", request);

        String id = request.getId();
        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();

        List<TransactionFetchResponse> list = dashboardService
            .fetchTransactionDetailLike(id, request.getOffset(),
                limit, request.getFrom(), request.getTo());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);
    }


    @PostMapping(Constants.URL.TRANSACTIONS_STATEMENT)
    public ResponseObject<List<TransactionFetchResponse>> fetchStatement(
        @Valid @RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);

        Integer limit =
            request.getLimit() == null ? Constants.MAX_STATEMENT_LIMIT : request.getLimit();
        limit = limit < 0 ? 0 : limit;
        if (limit > Constants.MAX_STATEMENT_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_STATEMENT_LIMIT);
            limit = Constants.MAX_STATEMENT_LIMIT;
        }
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        offset = offset < 0 ? 0 : offset;

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(request.getUserInfo().getTypeId(),
                request.getUserInfo().getUserId().toString(), request.getOffset(),
                limit, request.getFrom(), request.getTo());
        if (list.isEmpty()) {
            logger.debug(" no records found for  {}",  request.getUserInfo().getUserId());
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();
            throw new ResponseErrorObject(new ResponseObject(status, description,
                Arrays.asList(new Error(status, description)),
                null));
        }

        StatementResponse statementResponse = dashboardService
            .createStatement(request.getUserInfo().getUserId().toString(), request.getFrom(),
                request.getTo(),
                list);

        if ("MINI".equalsIgnoreCase(request.getType())) {
            SMSService.sendNotify(statementResponse);
        } else {

            String pin = Utils.generateSimpleAlphanumeric(5);

            byte[] pdf = emailService.generatePdf(statementResponse, pin);

            BaseModel data = new BaseModel();

            data.setAdditionalProperty("pin", pin);
            data.setAdditionalProperty("from", statementResponse.getCustomer().getFromFormatted());
            data.setAdditionalProperty("to", statementResponse.getCustomer().getToFormatted());
            data.setAdditionalProperty("name", statementResponse.getCustomer().getName());
            data.setAdditionalProperty("phone", statementResponse.getCustomer().getPhone());

            emailService.send(statementResponse.getCustomer().getUserId(),statementResponse.getCustomer().getTypeId(),
                statementResponse.getCustomer().getName(),Notify.Category.FULL_STATEMENT, data, pdf,
                    ".pdf");
        }

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            statementResponse);
    }

    @PostMapping(Constants.URL.ADMIN_TRANSACTIONS_STATEMENT)
    public ResponseObject<List<TransactionFetchResponse>> fetchAdminStatement(
        @Valid @RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);

        Integer limit =
            request.getLimit() == null ? Constants.MAX_STATEMENT_LIMIT : request.getLimit();
        limit = limit < 0 ? 0 : limit;
        if (limit > Constants.MAX_STATEMENT_LIMIT) {
            logger.info("limit requested too low. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_STATEMENT_LIMIT);
            limit = Constants.MAX_STATEMENT_LIMIT;
        }
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        offset = offset < 0 ? 0 : offset;

        String id = null;
        if (request.getUserId() != null) {
           id= request.getUserId().toString();
        }
        if (request.getMsisdn() != null) {
            id=request.getMsisdn();
        }

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(UserType.ADMIN, id, offset,
                limit, request.getFrom(), request.getTo());
        if (list.isEmpty()) {
            logger.debug(" no records found for  {}", id);
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();
            throw new ResponseErrorObject(new ResponseObject(status, description,
                Arrays.asList(new Error(status, description)),
                null));
        }


        StatementResponse statementResponse = dashboardService
            .createStatement(id, request.getFrom(), request.getTo(), list);

        if ("MINI".equalsIgnoreCase(request.getType())) {
            SMSService.sendNotify(statementResponse);
        } else {

            String pin = Utils.generateSimpleAlphanumeric(5);

            byte[] pdf = emailService.generatePdf(statementResponse, pin);

            BaseModel data = new BaseModel();

            data.setAdditionalProperty("pin", pin);
            data.setAdditionalProperty("from", statementResponse.getCustomer().getFromFormatted());
            data.setAdditionalProperty("to", statementResponse.getCustomer().getToFormatted());
            data.setAdditionalProperty("name", statementResponse.getCustomer().getName());
            data.setAdditionalProperty("phone", statementResponse.getCustomer().getPhone());

            emailService
                .send(statementResponse.getCustomer().getUserId(),
                    statementResponse.getCustomer().getTypeId(),
                    statementResponse.getCustomer().getName(), Notify.Category.FULL_STATEMENT, data,
                    pdf,
                    ".pdf");
        }

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            statementResponse);
    }


    @GetMapping(value = Constants.URL.ADMIN_TRANSACTIONS_STATEMENT_DOC,
        produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> fetchDoc(@RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Integer offset,
        @RequestParam(name = "user_id", required = false) String userId,
        @RequestParam(required = false) String msisdn,
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to) {

        limit = limit == null ? Constants.MAX_STATEMENT_LIMIT : limit;
        offset = offset == null ? 0 : offset;

        String id = null;
        if (userId != null) {
            id = userId;
        }
        if (msisdn != null) {
            id = msisdn;
        }

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(UserType.ADMIN, id, offset,
                limit, from, to);
        if (list.isEmpty()) {
            logger.debug(" no records found for  {}", id);
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();
            throw new ResponseErrorObject(new ResponseObject(status, description,
                Arrays.asList(new Error(status, description)),
                null));
        }

        StatementResponse statementResponse = dashboardService
            .createStatement(id, from, to, list);

        String pin = null;//Utils.generateSimpleAlphanumeric(5);

        byte[] pdf = emailService.generatePdf(statementResponse, pin);
        BaseModel data = new BaseModel();

        data.setAdditionalProperty("pin", pin);
        data.setAdditionalProperty("from", statementResponse.getCustomer().getFromFormatted());
        data.setAdditionalProperty("to", statementResponse.getCustomer().getToFormatted());
        data.setAdditionalProperty("name", statementResponse.getCustomer().getName());
        data.setAdditionalProperty("phone", statementResponse.getCustomer().getPhone());
        emailService
            .send(statementResponse.getCustomer().getUserId(),
                statementResponse.getCustomer().getTypeId(),
                statementResponse.getCustomer().getName(), Notify.Category.FULL_STATEMENT, data,
                pdf,
                ".pdf");

        //    ByteArrayResource resource = new ByteArrayResource(pdf);
        //   InputStreamResource inputStreamResource = new InputStreamResource(pdf);


        return ResponseEntity.ok()
            // Content-Disposition
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + id + "-"+Utils.FORMATTER_DAY.format(Instant.now().atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime())+
                "-statement.pdf")
            // Content-Type
            .contentType(MediaType.APPLICATION_OCTET_STREAM) //
            // Content-Lengh
            //   .contentLength(resource.contentLength()) //
            .body(pdf);

    }


    @PostMapping(Constants.URL.PARTNER_TRANSACTIONS_FETCH)
    public ResponseObject<List<TransactionFetchResponse>> partnerFetch(@Valid @RequestBody TransactionFetchRequest request) {

        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }

        Integer offset = request.getOffset() == null ? 0 : request.getOffset();
        String userId = request.getUserInfo().getUserId().toString();
        if (request.getUserInfo().getAgentId() != null) {
            userId = request.getUserInfo().getAgentId().toString();
        }

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(request.getUserInfo().getTypeId(), userId
                , request.getOffset(),
                limit, request.getFrom(), request.getTo());
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);


    }



    @PostMapping(Constants.URL.TRANSACTIONS_ID)
    public ResponseObject<TransactionFetchResponse> fetchTransactionByTransactionId(@RequestBody TransactionFetchRequest request) {
        //   try {
        // logger.info(" {} ", request);
        //only retrieve your data
        Optional<Transaction> optional = Optional.empty();
        if (request.getId() != null) {
            optional = crudServiced.fetchTransactionById(request.getId());

        } else if (request.getTransactionId() != null) {
            optional = crudServiced.fetchTransactionByTransactionId(request.getTransactionId());

        } else if (request.getReference() != null) {

            List<Transaction> list = crudServiced.fetchTransactionByReference(request.getReference());
            optional = list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));


        }
        Optional<Transaction> optional2 = optional.filter(transaction -> {
            if (!(Arrays.asList(UserType.ADMIN, UserType.PARTNER).contains(request.getUserInfo().getTypeId()))
                    && !transaction.getUserInfo().getUserId().equals(request.getUserInfo().getUserId())) {
                logger.info("filter out this transaction  {} {} ", transaction.getId(), request.getUserInfo().getUserId());
                return false;
            }
            return true;
        });

        if (optional2.isPresent()) { //owner of tx.. sow all dest
            optional = optional2;
        } else {// check if hes in dest and filter just his tx
            Optional<Transaction> finalOptional = optional;
            List<Destination> list = !optional.isPresent() ? List.of() : optional.get().getDestinations().stream().filter(d -> {//filter in this transaction.destinations coz user part of destination
                return request.getUserInfo().getUserId().toString().equals(d.getPayload().getAccount().getUserId().toString())
                        && TransactionStatus.SUCCESS.equals(d.getTransactionStatus());//only successful ones
            }).collect(Collectors.toList());

            if (list.isEmpty()) {
                optional = Optional.empty();
            } else {//just show his destinations
                optional.get().getDestinations().clear();
                optional.get().getDestinations().addAll(list);
            }

        }

        TransactionFetchResponse[] t = new TransactionFetchResponse[1];
        optional.ifPresent(transaction -> {
            t[0] = TransactionFetchResponse.from(transaction);
        });
        if (t[0] == null) {//no transaction found
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type,
                    ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.TRANSACTION_NOT_FOUND.type, ResponseCode.TRANSACTION_NOT_FOUND.name()))
                    , null));//NestedExceptionUtils.getMostSpecificCause( e));
        }
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, t[0]);
//        } catch (Exception e) {
//            logger.error("", e);
//            throw new ResponseErrorObject( new ResponseObject(ResponseCode.FAILURE.type,
//                    ResponseCode.FAILURE.name(),
//                    Arrays.asList(new Error(ResponseCode.TRANSACTION_NOT_FOUND.type, ResponseCode.TRANSACTION_NOT_FOUND.name()))
//                    , null));
//        }
    }

    @PostMapping(Constants.URL.PARTNER_TRANSACTIONS_ID)
    public ResponseObject<TransactionFetchResponse> fetchPartnerTransactionByTransactionId(@RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);
        //only retrieve your data
        Optional<Transaction> optional = Optional.empty();
        if (request.getTransactionId() != null) {
            optional = crudServiced.fetchTransactionByTransactionId(request.getTransactionId());

        } else if (request.getReference() != null) {
            List<Transaction> l = crudServiced.fetchTransactionByReference(request.getReference());
            optional = l.isEmpty() ? Optional.empty() : Optional.of(l.get(0));
        } else if (request.getId() != null) {
            optional = crudServiced.fetchTransactionById(request.getId());
        }

        Optional<Transaction> optional2 = optional.isPresent() ? Optional.empty() : optional.filter(transaction -> {
            //TODO: check for partner records only
//            if (transaction.getPayload().getChargeInfo().getPartnerInfo() != null
//                    && transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getUserId() != null && !request.getUserInfo().getUserId().equals(transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getUserId())) {
//                logger.info("filter out this transaction  {} userId {} ", transaction.getId(), request.getUserInfo().getUserId());
//                return false;
//            }
            return true;
        });

        // check if hes in dest and filter just his tx
        if (optional.isPresent()) {

            if (!request.getUserInfo().getUserId().equals(optional.get().getUserInfo().getUserId())) {//not his request
                List<Destination> list = optional.get().getDestinations().stream().filter(d -> {//filter in this transaction.destinations coz user part of destination
                    return request.getUserInfo().getUserId().equals(d.getPayload().getAccount().getUserId()) &&
                            (TransactionStatus.PROCESSING.equals(d.getTransactionStatus()) ||
                                    TransactionStatus.SUCCESS.equals(d.getTransactionStatus())
                            );
                    //filter out failed transactions as delivery
                }).collect(Collectors.toList());
                if (!list.isEmpty()) {//just show where he is a recepient
                    optional.get().getDestinations().clear();
                    optional.get().getDestinations().addAll(list);
                } else {
                    logger.info("filter out this transaction  {} userId {} requester {} ", optional.get().getId(), request.getUserInfo().getUserId(), request.getUserInfo().getUserId());
                    optional = Optional.empty();
                }
            }
        }


        TransactionFetchResponse[] t = new TransactionFetchResponse[1];
        optional.ifPresent(transaction -> {
            t[0] = TransactionFetchResponse.from(transaction);
            t[0].setRailRevenue(null);

            //if owner of tx
            if(request.getUserInfo().getUserId().equals(t[0].getUserId())){
                t[0].getDestination().stream().forEach(d->{
                    d.setAvailableBalance(null);
                });
               if(t[0].getReversalChild() !=null) {
                   t[0].getReversalChild().getDestination().stream().forEach(d->{
                       d.setAvailableBalance(null);
                   });
               }
            }else{
                t[0].getSource().stream().forEach(d->{
                    d.setAvailableBalance(null);
                });
                if(t[0].getReversalChild() !=null) {
                    t[0].getReversalChild().getSource().stream().forEach(d->{
                        d.setAvailableBalance(null);
                    });
                }
            }

        });
        if (t[0] == null) {//no transaction found
            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type,
                    ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.TRANSACTION_NOT_FOUND.type, ResponseCode.TRANSACTION_NOT_FOUND.name()))
                    , null));//NestedExceptionUtils.getMostSpecificCause( e));
        }

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, t[0]);
    }

    @PostMapping(Constants.URL.TRANSACTIONS_STATUS_ID)
    public ResponseObject<TransactionFetchResponse> fetchTransactionStatusByTransactionId(@RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);
        ResponseObject<TransactionFetchResponse> res = fetchTransactionByTransactionId(request);


        if (res.getData() != null) {
            TransactionFetchResponse filtered = new TransactionFetchResponse();
            filtered.setStatus(res.getData().getStatus());
            filtered.setCode(res.getData().getCode());
            filtered.setReason(res.getData().getReason());
            res.setData(filtered);
        } else {

            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type,
                    ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.TRANSACTION_NOT_FOUND.type, ResponseCode.TRANSACTION_NOT_FOUND.name()))
                    , null));//NestedExceptionUtils.getMostSpecificCause( e));

        }

        return res;
    }

    @PostMapping(Constants.URL.PARTNER_TRANSACTIONS_STATUS_ID)
    public ResponseObject<TransactionFetchResponse> fetchPartnerTransactionStatusByTransactionId(@RequestBody TransactionFetchRequest request) {
        //  logger.info(" {} ", request);
        ResponseObject<TransactionFetchResponse> res = fetchPartnerTransactionByTransactionId(request);
        if (res.getData() != null) {
            TransactionFetchResponse filtered = new TransactionFetchResponse();
            filtered.setStatus(res.getData().getStatus());
            filtered.setCode(res.getData().getCode());
            filtered.setReason(res.getData().getReason());
            res.setData(filtered);
        } else {

            throw new ResponseErrorObject(new ResponseObject(ResponseCode.FAILURE.type,
                    ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.TRANSACTION_NOT_FOUND.type, ResponseCode.TRANSACTION_NOT_FOUND.name()))
                    , null));//NestedExceptionUtils.getMostSpecificCause( e));

        }
        return res;
    }

}
