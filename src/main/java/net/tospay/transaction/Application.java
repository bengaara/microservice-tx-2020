package net.tospay.transaction;

import io.sentry.Sentry;
import java.util.Optional;
import net.tospay.transaction.entities.TransactionConfig;
import net.tospay.transaction.repositories.BaseRepository;
import net.tospay.transaction.repositories.TransactionConfigRepository;
import net.tospay.transaction.services.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication//(scanBasePackages = { "net.tospay.*.*" })
@EnableAsync
@EnableScheduling
//@EnableConfigurationProperties
@EnableJpaRepositories( basePackages={ "net.tospay.transaction.repositories" },repositoryBaseClass = BaseRepository.class)
//@EnableAutoConfiguration
//@EntityScan("net.tospay.transaction.entities")
@EnableJpaAuditing
public class Application implements CommandLineRunner
{

    @Value("${version}")
    String version;

    @Value("${spring.profiles.active}")
    String profiles;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    LicenseService licenseService;

    @Autowired
    TransactionConfigRepository transactionConfigRepository;

    public static void main(String[] args)
    {

    //    System.setProperty("sentry.properties.file", "application.properties");
        SpringApplication.run(Application.class, args);

    }

    @Override
    public void run(String... arg0) throws Exception {
        logger.debug("application started  version {} profiles {}", version, profiles);

        licenseService.publishDetails();
        Optional<TransactionConfig> opt = transactionConfigRepository.findLatestConfig();
        TransactionConfig transactionConfig = opt
            .orElse(transactionConfigRepository.save(TransactionConfig.init()));

    }


}
