package net.tospay.transaction.services;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Transaction;

//scheduler should not be a bean @Component
public class JobScheduleService extends BaseService
{
    ObjectMapper mapper = new ObjectMapper();

    FundService fundService;

    CrudService crudService;

    ReportingService reportingService;

    public JobScheduleService(FundService fundService, CrudService crudService, ReportingService reportingService)
    {
        this.fundService = fundService;
        this.crudService = crudService;
        this.reportingService = reportingService;
    }

    //  @Scheduled(cron ="${cron.job.autoreversal}")
    @Transactional
    public void checkFailedTransactions()
    {
        try {
            logger.info("Cron Task :: checkFailedTransactions  Execution Time - {} {}", LocalDateTime.now(),
                    Thread.currentThread().getName());
            LocalDateTime now = LocalDateTime.now(); // current date and time
            LocalDateTime midnight = now.toLocalDate().atStartOfDay();

            List<Transaction> list = crudService.fetchFailedSourcedTransactions(midnight);

            list.forEach(transaction -> {
                fundService.refundFloatingFundsToWallet(transaction);
            });
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    @Scheduled(cron = "${cron.job.kpa_report}", zone = "Africa/Nairobi")
    public void prepareKPAReports()
    {
        try {
            logger.info("Cron Task :: prepareKPAReports  Execution Time - {} {}", LocalDateTime.now(),
                    Thread.currentThread().getName());
            LocalDateTime toNow = LocalDateTime.now(); // current date and time
            LocalDateTime midnight = toNow.toLocalDate().atStartOfDay();

            reportingService.prepareKPAReports(toNow);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }
}
