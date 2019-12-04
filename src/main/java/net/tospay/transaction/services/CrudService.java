package net.tospay.transaction.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import net.tospay.transaction.entities.BaseEntity;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.OffsetBasedPageRequest;
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

    public @NotNull ArrayList<Source> fetchSources(UUID userId, UserType userType,Integer offset)
    {
        try {

            logger.info(" {} {}", userId,userType);
         //   Sort.TypedSort<Source> s=Sort.sort(Source.class);
         //   Sort sort = s.by(Source::getDateModified).descending();
            return sourceRepository.findByUserIdAndUserType(userId, userType,
                    new OffsetBasedPageRequest(offset==null?0:offset,PAGE_SIZE,Sort.by(BaseEntity.toDbField(Destination.DATE_CREATED)).descending()));
        } catch (Exception e){
            logger.error(" {}", e);
            return new ArrayList<Source>();
        }
    }


    public @NotNull ArrayList<Destination> fetchDestinations(UUID userId, UserType userType,Integer offset)
    {
        try {
            logger.info(" {} {}", userId,userType);
            return destinationRepository.findByUserIdAndUserType(userId, userType,
                    new OffsetBasedPageRequest(offset==null?0:offset,PAGE_SIZE,Sort.by(BaseEntity.toDbField(Destination.DATE_CREATED)).descending()));

        } catch (Exception e) {
            logger.error(" {}", e);
            return new ArrayList<Destination>();
        }
    }
    public @NotNull Optional<net.tospay.transaction.entities.Transaction> fetchTransactionByTransactionId(String transactionId)
    {
        try {
            logger.info(" {}", transactionId);
            return transactionRepository.findByTransactionId(transactionId);
        } catch (Exception e) {
            logger.error(" {}", e);
            return Optional.empty();
        }
    }

    public @NotNull List<net.tospay.transaction.entities.Transaction> fetchFailedTransactions(LocalDateTime midnight)
    {
        try {
            logger.info("fetchFailedTransactions");

            return transactionRepository.findByStatusAndDate(TransactionStatus.FAILED,midnight);
        } catch (Exception e) {
            logger.error(" {}", e);
            return new ArrayList<>();
        }
    }

}
