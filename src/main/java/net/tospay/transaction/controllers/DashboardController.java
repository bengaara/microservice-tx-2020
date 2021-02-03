package net.tospay.transaction.controllers;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import net.tospay.transaction.entities.Revenue;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.request.DashboardRequest;
import net.tospay.transaction.models.request.TransactionFetchRequest;
import net.tospay.transaction.models.response.DashboardTransactionSummary;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionDashboardFetchResponse;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.DashboardService;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class DashboardController extends BaseController {

    CrudService crudServiced;

    DashboardService dashboardService;

    TransactionRepository transactionRepository;


    UUID partnerId = UUID
        .fromString("5b56b8dc-fa47-4494-b00a-7f6421248dc5");//TODO:remove when auth fixed

    public DashboardController(CrudService crudServiced, DashboardService dashboardService,
        TransactionRepository transactionRepository) {
        this.crudServiced = crudServiced;
        this.dashboardService = dashboardService;
        this.transactionRepository = transactionRepository;
    }

    //used by  merchant
    @PostMapping(Constants.URL.PARTNER_DASHBOARD_TRANSACTIONS_REVENUE)
    public ResponseObject<List<TransactionRepository.TransactionSummary>> partnerFetchTransactionRevenue(
        @RequestBody DashboardRequest request) {
        // logger.info(" {} ", request);
        UUID partner = request.getUserInfo().getPartnerId();
        if (partner == null) {
            logger.info("setting default partner: {}", partnerId);
            partner = partnerId;
        }
        String currency = request.getCurrency();
        String userId = request.getUserId();

        LocalDateTime to = request.getTo() == null ? LocalDateTime.now() : request.getTo().atStartOfDay(); // chosen date and time
        LocalDateTime from = request.getFrom() == null ? to.minusDays(7) : request.getFrom().atStartOfDay();
        final long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDateTime beforeFrom = from.minusDays(days);


        List<TransactionRepository.TransactionSummary>[] list = new ArrayList[2];
        list[0] = dashboardService.weeklyTransactionSummaryByPartnerId(partner,userId,currency, beforeFrom, from);
        list[1] = dashboardService.weeklyTransactionSummaryByPartnerId(partner,userId,currency, from, to);

        DashboardTransactionSummary dashboardTransactionSummary = dashboardService.mapRevenue(partner, from, to,
                list);


        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, dashboardTransactionSummary);

    }

    @PostMapping(Constants.URL.PARTNER_DASHBOARD_TRANSACTIONS_REVENUE_TABLE)
    public ResponseObject<List<TransactionRepository.TransactionSummary>> partnerFetchTransactionRevenueTable(@RequestBody DashboardRequest request) {
        // logger.info(" {} ", request);
        UUID partner = request.getUserInfo().getPartnerId();
        if (partner == null) {
            logger.info("setting default partner: {}", partnerId);
            partner = partnerId;
        }
      //  String userId = request.getUserId();
        LocalDateTime to = request.getTo() == null ? LocalDateTime.now() : request.getTo().atStartOfDay(); // chosen date and time
        LocalDateTime from = request.getFrom() == null ? to.minusDays(7) : request.getFrom().atStartOfDay();
        //  final long days = ChronoUnit.DAYS.between(from, to);
        //  LocalDateTime beforeFrom = from.minusDays(days);

        DashboardRequest.GroupType groupType = request.getGroupType() == null ? DashboardRequest.GroupType.DAY : request.getGroupType();
        Integer limit = request.getLimit() == null ? 10 : request.getLimit();
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();

        List<Revenue> list = transactionRepository.findRevenueByPartnerId(partner, from, to, limit, offset,groupType.name());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, list);

    }




    //used by  merchant, and admin
    @PostMapping(Constants.URL.DASHBOARD_TRANSACTIONS_REVENUE)
    public ResponseObject<List<TransactionRepository.TransactionSummary>> fetchTransactionRevenue(@RequestBody DashboardRequest request) {
        //   logger.info(" {} ", request);
//        LocalDateTime toNow = LocalDateTime.now(); // current date and time
//        LocalDateTime midnight = toNow.toLocalDate().atStartOfDay();
//        LocalDateTime lastWeek = midnight.minusDays(7);
//        LocalDateTime last2Week = midnight.minusDays(14);
        LocalDateTime to = request.getTo() == null ? LocalDateTime.now() : request.getTo().atStartOfDay(); // chosen date and time
        to = to.toLocalDate().atStartOfDay();
        LocalDateTime from = request.getFrom() == null ? to.minusDays(7) : request.getFrom().atStartOfDay();
        final long days = ChronoUnit.DAYS.between(from, to);
        LocalDateTime beforeFrom = from.minusDays(days);


        List<TransactionRepository.TransactionSummary>[] list = new ArrayList[2];
        list[0] = dashboardService.weeklyTransactionSummary(request.getUserInfo().getUserId(), beforeFrom, from);
        list[1] = dashboardService.weeklyTransactionSummary(request.getUserInfo().getUserId(), from, to);

        DashboardTransactionSummary dashboardTransactionSummary = dashboardService.mapRevenue(request.getUserInfo().getUserId(), from, to, list);

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, dashboardTransactionSummary);
    }

    @PostMapping(Constants.URL.PARTNER_DASHBOARD_TRANSACTIONS_ID)
    public ResponseObject<TransactionDashboardFetchResponse> fetchPartnerDashboardTransactionByTransactionId(@RequestBody TransactionFetchRequest request) {
        // logger.info(" {} ", request);
        //only retrieve your data
        Optional<Transaction> optional = Optional.empty();
        if (request.getTransactionId() != null) {
            optional = crudServiced.fetchTransactionByTransactionId(request.getTransactionId());

        } else if (request.getReference() != null) {
            List<Transaction> lt = crudServiced.fetchTransactionByReference(request.getReference());
            optional = lt.isEmpty() ? Optional.empty() : Optional.of(lt.get(0));
        } else if (request.getId() != null) {
            optional = crudServiced.fetchTransactionById(request.getId());
        }
        optional = optional.filter(transaction -> {
            if (transaction.getPayload().getChargeInfo().getPartnerInfo()!= null
                    && transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getUserId() != null && !request.getUserInfo().getUserId().equals(transaction.getPayload().getChargeInfo().getPartnerInfo().getAccount().getUserId())) {
                logger.info("filter out this transaction  {} userId {} ", transaction.getId(), request.getUserInfo().getUserId());
                return false;
            }
            return true;
        });

        TransactionDashboardFetchResponse[] t = new TransactionDashboardFetchResponse[1];
        optional.ifPresent(transaction -> {
            t[0] = TransactionDashboardFetchResponse.from(transaction);
        });
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            t[0]);
    }

    @PostMapping(Constants.URL.PARTNER_DASHBOARD_TRANSACTIONS_FETCH_USER)
    public ResponseObject<List<TransactionFetchResponse>> fetchUserTransactions(
        @RequestBody TransactionFetchRequest request) {
        //logger.info(" {} ", request);

        Integer limit = request.getLimit() == null ? Constants.MAX_FETCH_LIMIT : request.getLimit();
        if (limit > Constants.MAX_FETCH_LIMIT) {
            logger.info("limit requested too high. setting it to MAX_FETCH_LIMIT {}",
                Constants.MAX_FETCH_LIMIT);
            limit = Constants.MAX_FETCH_LIMIT;
        }
        Integer offset = request.getOffset() == null ? 0 : request.getOffset();

        String userId = request.getUserId().toString();

        List<TransactionFetchResponse> list = dashboardService
            .fetchFilteredTransaction(UserType.PARTNER, userId,
                request.getOffset(),
                limit, request.getFrom(), request.getTo());
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);
    }

    @PostMapping(Constants.URL.PARTNER_DASHBOARD_TRANSACTIONS_FETCH)
    public ResponseObject<List<TransactionFetchResponse>> partnerDashboardFetch(
        @Valid @RequestBody TransactionFetchRequest request) {
        // logger.info(" {}", request);
        UUID partner = request.getUserInfo().getPartnerId();
        if (partner == null) {
            logger.info("setting default partner: {}", partnerId);
            partner = partnerId;
        }

        List<TransactionFetchResponse> list = dashboardService
            .fetchPartnerTransaction(partner, request.getOffset(),
                request.getLimit(), request.getFrom(), request.getTo());
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);
    }

    @PostMapping(URL.ADMIN_TRANSACTIONS_FETCH)
    public ResponseObject<List<TransactionFetchResponse>> adminFetch(
        @Valid @RequestBody TransactionFetchRequest request) {
        // logger.info(" {}", request);

        Integer limit = request.getLimit();
        Integer offset = request.getOffset();

        String id = null;
        if (request.getUserId() != null) {
            id = Objects.toString(request.getUserId());
        }
        if (request.getMsisdn() != null) {
            id = request.getMsisdn();
        }
        List<TransactionFetchResponse> list = new ArrayList<>();
        if (id == null) {//fetch all

            list = dashboardService
                .fetchAllTransaction(offset,
                    limit, request.getFrom(), request.getTo());
        } else {
            list = dashboardService
                .fetchFilteredTransaction(UserType.ADMIN, id, offset,
                    limit, request.getFrom(), request.getTo());
        }

        if (list.isEmpty()) {
            logger.debug(" no records found for  {}", id);
            String status = ResponseCode.FAILURE.type;
            String description = ResponseCode.FAILURE.name();
            throw new ResponseErrorObject(new ResponseObject(status, description,
                Arrays.asList(new Error(status, description)),
                null));
        }
        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null,
            list);
    }
}
