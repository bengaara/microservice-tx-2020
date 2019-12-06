package net.tospay.transaction.services;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Transaction;

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
            logger.info("Cron Task :: checkFailedTransactions  Execution Time - {} {}", LocalDateTime.now(),Thread.currentThread().getName());
            LocalDateTime now = LocalDateTime.now(); // current date and time
            LocalDateTime midnight = now.toLocalDate().atStartOfDay();

            List<Transaction> list =  crudService.fetchFailedTransactions(midnight);

            list.forEach(transaction -> {
                fundService.refundFloatingFundsToWallet(transaction);
            });



        } catch (Exception e) {
            logger.error("{}", e);
        }
    }
}
