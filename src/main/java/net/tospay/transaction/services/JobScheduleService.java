package net.tospay.transaction.services;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.MobilePayAction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.request.Account;
import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.request.PaymentSplitRequest;
import net.tospay.transaction.models.request.PaymentSplitResponse;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.request.TransferRequest;
import net.tospay.transaction.models.response.ChargeResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.MerchantInfo;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransferIncomingResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Component
public class JobScheduleService extends BaseService
{
    ObjectMapper mapper = new ObjectMapper();


    @Autowired
    FundService fundService;


    @Autowired CrudService crudService;



    public JobScheduleService( FundService fundService,CrudService crudService)
    {
        this.fundService = fundService;
        this.crudService = crudService;
    }


    @Scheduled(cron ="${cron.job.autoreversal}")
    @Transactional
    public void checkFailedTransactions()
    {
        try {
            logger.info("Cron Task :: checkFailedTransactions  Execution Time - {}", LocalDateTime.now());
            LocalDateTime now = LocalDateTime.now(); // current date and time
            LocalDateTime midnight = now.toLocalDate().atStartOfDay();

            List<Transaction> list =  crudService.fetchFailedTransactions(midnight);

            list.forEach(transaction -> {
                fundService.moveFundsToWallet(transaction);
            });



        } catch (Exception e) {
            logger.error("{}", e);
        }
    }
}
