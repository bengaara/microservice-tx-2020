package net.tospay.transaction.services;

import java.util.ArrayList;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class CrudService extends BaseService
{
    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    public CrudService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository)
    {
        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    public @NotNull ArrayList<Source> fetchSources(UUID userId, AccountType userType)
    {
        try {
            logger.info(" {} {}", userId,userType);
            return sourceRepository.fetchByUserIdAndUserType(userId, userType);
        } catch (Exception e) {
            logger.error(" {}", e);
            return new ArrayList<Source>();
        }
    }

    public @NotNull ArrayList<Destination> fetchDestinations(UUID userId, AccountType userType)
    {
        try {
            logger.info(" {} {}", userId,userType);
            return destinationRepository.fetchByUserIdAndUserType(userId, userType);
        } catch (Exception e) {
            logger.error(" {}", e);
            return new ArrayList<Destination>();
        }
    }
}
