package net.tospay.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import net.tospay.transaction.repositories.BaseRepository;

@SpringBootApplication(scanBasePackages = { "net.tospay.*.*" })
//@EnableConfigurationProperties
@EnableJpaRepositories( basePackages={ "net.tospay.*.*" },repositoryBaseClass = BaseRepository.class)
@EntityScan("net.tospay.*.*")

public class TransactionApplication implements CommandLineRunner
{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void run(String... arg0) throws Exception {
        logger.debug("application started ");
    }

    public static void main(String[] args)
    {
        SpringApplication.run(TransactionApplication.class, args);

    }
}
