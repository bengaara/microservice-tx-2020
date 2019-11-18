package net.tospay.transaction.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")
@Component
public class AsyncConfig
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(1000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Async-");
        return executor;
    }
}
