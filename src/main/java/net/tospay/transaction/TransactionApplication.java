package net.tospay.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "net.tospay.*.*" })
//@EnableConfigurationProperties
@EnableJpaRepositories({ "net.tospay.*.*" })
@EntityScan("net.tospay.*.*")
@EnableScheduling
@EnableAsync
public class TransactionApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(TransactionApplication.class, args);
    }
}
