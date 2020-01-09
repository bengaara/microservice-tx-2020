package net.tospay.transaction.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.JobScheduleService;
import net.tospay.transaction.services.ReportingService;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired FundService fundService;

    @Autowired CrudService crudService;

    @Autowired ReportingService reportingService;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler()
    {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    public JobScheduleService jobScheduleService()
    {
        JobScheduleService jobScheduleService = new JobScheduleService(fundService, crudService, reportingService);

        return jobScheduleService;
    }
}
