package net.tospay.transaction.configs;

import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.services.ReportingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")

@Configuration
public class AsyncConfig implements SchedulingConfigurer {
    private final int POOL_SIZE = 10;
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    FundService fundService;

    @Autowired
    CrudService crudService;

    @Autowired
    ReportingService reportingService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
        threadPoolTaskScheduler.setThreadNamePrefix("my-scheduled-task-pool-");
        threadPoolTaskScheduler.initialize();

        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
    }




    //    @Bean
//    public ThreadPoolTaskScheduler threadPoolTaskScheduler()
//    {
//        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
//        threadPoolTaskScheduler.setPoolSize(5);
//        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
//        return threadPoolTaskScheduler;
//    }
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(500);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("taskExecutor-");
        executor.initialize();
        return executor;
    }

}
