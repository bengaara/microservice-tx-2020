package net.tospay.transaction.services;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import net.tospay.transaction.entities.BaseEntity;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class CrudService extends BaseService
{
    private static final int PAGE_SIZE = 10;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;
    TransactionRepository transactionRepository;

    public CrudService(SourceRepository sourceRepository, DestinationRepository destinationRepository,TransactionRepository transactionRepository)
    {
        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
        this.transactionRepository = transactionRepository;
    }

    public @NotNull ArrayList<Source> fetchSources(UUID userId, AccountType userType)
    {
        try {

            logger.info(" {} {}", userId,userType);
         //   Sort.TypedSort<Source> s=Sort.sort(Source.class);
         //   Sort sort = s.by(Source::getDateModified).descending();
            return sourceRepository.findByUserIdAndUserType(userId, userType,
                    PageRequest.of(0, PAGE_SIZE, Sort.by(BaseEntity.toDbField(Source.DATE_CREATED)).descending()));
        } catch (Exception e){
            logger.error(" {}", e);
            return new ArrayList<Source>();
        }
    }


    public @NotNull ArrayList<Destination> fetchDestinations(UUID userId, AccountType userType)
    {
        try {
            logger.info(" {} {}", userId,userType);
            return destinationRepository.findByUserIdAndUserType(userId, userType,
                    PageRequest.of(0, PAGE_SIZE, Sort.by(BaseEntity.toDbField(Destination.DATE_CREATED)).descending()));
        } catch (Exception e) {
            logger.error(" {}", e);
            return new ArrayList<Destination>();
        }
    }
    public @NotNull Optional<Transaction> fetchTransactionByTransactionId(String transactionId)
    {
        try {
            logger.info(" {}", transactionId);
            return transactionRepository.findByTransactionId(transactionId);
        } catch (Exception e) {
            logger.error(" {}", e);
            return Optional.empty();
        }
    }
}
