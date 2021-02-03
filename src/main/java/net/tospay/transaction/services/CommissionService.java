package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import net.tospay.transaction.entities.Commission;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.AccountType.AccountSubType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.models.request.CommissionRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.CommissionResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.CommissionRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class CommissionService extends BaseService {


    @Autowired //TODO bug dont remove - when method is called asyn they r needed
            TransactionRepository transactionRepository;

    CommissionRepository commissionRepository;

    @Value("#{${STORE_PAY_URLS}}")
    Map<String, String> STORE_PAY_URLS;

    @Value("#{${STORE_STATUS_URLS}}")
    Map<String, String> STORE_STATUS_URLS;

    @Value("${commission.url}")
    String commissionUrl;


    public CommissionService(RestTemplate restTemplate, TransactionRepository transactionRepository,
        CommissionRepository commissionRepository) {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

         this.commissionRepository =  commissionRepository;

    }


    @Async
    // @Transactional - dont do transactional
    public CompletableFuture pullCommission(Transaction transaction,FundService fundService) {
        try {
            logger.debug("pullCommission {}", transaction.getId());

            if (!TransactionStatus.SUCCESS.equals(transaction.getTransactionStatus())) {
                logger.debug("only pay commissions for successful tx {} {} ",transaction.getId(),transaction.getTransactionStatus());
                return CompletableFuture.completedFuture(null);
            }
            Optional<Commission> opt = commissionRepository.findById(transaction.getId());

           if(opt.isPresent()){
               logger.debug("only pay non processed commissions tx {} {} ",transaction.getId());
               return CompletableFuture.completedFuture(null);
           }


            AtomicBoolean commissionPreProcessedFull = new AtomicBoolean(true);
            Account  account = null;
            if(UserType.AGENT.equals(transaction.getPayload().getSource().get(0).getAccount().getUserType())){
                account = transaction.getPayload().getSource().get(0).getAccount();
            }
            if(UserType.AGENT.equals(transaction.getPayload().getDelivery().get(0).getAccount().getUserType())){
                account = transaction.getPayload().getSource().get(0).getAccount();
            }

            if(account ==null){
                logger.debug("only pay commissions for agent tx  {} {} ",transaction.getId(),transaction.getTransactionStatus());
                return CompletableFuture.completedFuture(null);
            }
// Hibernate.initialize(transactionRepository);transactionRepository.refresh(transaction);


            CommissionRequest commissionRequest = new CommissionRequest();
            commissionRequest.setAmount(transaction.getPayload().getOrderInfo().getAmount());

            commissionRequest.setUserType(account.getUserType());
            commissionRequest.setUserId(account.getUserId());
            commissionRequest.setWalletId(UUID.fromString(account.getId()));
            commissionRequest.setTransactionDate(transaction.getDateCreated().toString());
            commissionRequest.setTransactionSubType(transaction.getPayload().getOrderInfo().getType().name());
            commissionRequest.setTransactionType(transaction.getType());
         //   commissionRequest.setTransactionId(transaction.getTransactionId());

            Commission commission= new Commission();
            commission.setId(transaction.getId());
            commission.setAmount(commissionRequest.getAmount());
            commission.setPayload(commissionRequest);

            commissionRepository.save(commission);
            ResponseObject<List<CommissionResponse>> response = this.hitCommission(commissionRequest);

            commission.setFetchStatus(ResponseCode.valueOfType(response.getStatus()));

            if (!ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus())) {
                logger.debug("commission lookup failed {} {} ",transaction.getId(),response.getStatus());

                return CompletableFuture.completedFuture(null);
            }
            commission.setCommissionResponse(response.getData());
            response.getData().forEach(commissionData -> {
                //if (BigDecimal.ZERO.compareTo(commissionData.getAmount().getAmount())<=0) { logger.debug("zero commission skipping ... {} {} ",commissionData);

                    TransferOutgoingRequest request = new TransferOutgoingRequest();
                    Account a = new Account();
                    a.setType(AccountType.WALLET);
                    a.setSubType(AccountSubType.COMMISSION);
                    a.setId(commissionData.getWalletId().toString());
                    a.setUserId(commissionData.getUserId());
                    a.setUserType(commissionData.getUserType());
                    request.setAccount(a);
                    request.setTransactionType(TransactionType.TRANSFER);

                    request.setAction("DESTINATION");
                    request.setAmount(commissionData.getAmount());
                    request.setExternalReference(commission.getId());
                    request.setDescription("Commission");
                 //   commission.getAgents().add(commissionData);

                    ResponseObject<StoreResponse> res = fundService.hitStore(request.getAccount().getType(), request);
                    commission.getStoreResponses().add(res);
                    if (ResponseCode.FAILURE.type.equalsIgnoreCase(res.getStatus())) {//failure
                        logger.debug("commission setProcessStatus ... {} {} ",commission.getId(),res.getStatus());
                        commission.setProcessStatus(ResponseCode.valueOfType(res.getStatus()));
                        commissionPreProcessedFull.set(false);
                        commission.getFailedRequests().add(request);
                        commission.setFailed(true);
                    }
            });

            transaction.setCommissionPreProcessedFull(commissionPreProcessedFull.get());

            transactionRepository.save(transaction);
            commissionRepository.save(commission);
            return CompletableFuture.completedFuture(null);//Pair.of(transaction, list)
        } catch (Exception e) {
            logger.error("", e);
            return CompletableFuture.completedFuture(null);

        }
    }

    public ResponseObject<List<CommissionResponse>> hitCommission( CommissionRequest request) {
        try {
            String url = commissionUrl;
            this.logger.debug("hitCommission request: {} {}", url, request);//.setHideData(false)

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<CommissionRequest>(request, headers);

            ResponseEntity<ResponseObject<List<CommissionResponse>>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<ResponseObject<List<CommissionResponse>>>() {
            });
            this.logger.debug("hitCommission response: {}", response);

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
            String description = e.getLocalizedMessage();
            description = description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }
}
