package net.tospay.transaction.services;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.OffsetBasedPageRequest;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CrudService extends BaseService {

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    TransactionRepository transactionRepository;

    public CrudService(SourceRepository sourceRepository, DestinationRepository destinationRepository,
                       TransactionRepository transactionRepository) {
        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
        this.transactionRepository = transactionRepository;
    }


    public @NotNull List<Source> fetchSources(UUID userId, Integer offset, Integer limit) {
        try {

            logger.info(" {}", userId);
            //   Sort.TypedSort<Source> s=Sort.sort(Source.class);
            //   Sort sort = s.by(Source::getDateModified).descending();
            if (userId == null) {
                Pageable p = new OffsetBasedPageRequest(offset, limit, Sort.by(Source.DATE_CREATED).descending());
                Page page = sourceRepository.findAll(p);
                return page.getContent();
            } else {
                return sourceRepository.findByUserId(userId,
                        new OffsetBasedPageRequest(offset, limit,
                                Sort.by(Source.DATE_CREATED).descending()));
            }

        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Source>();
        }
    }


    public @NotNull List<Destination> fetchDestinations(UUID userId, Integer offset, Integer limit) {
        try {
            logger.info(" {}", userId);
            if (userId == null) {
                Pageable p = new OffsetBasedPageRequest(offset, limit, Sort.by(Source.DATE_CREATED).descending());
                Page page = destinationRepository.findAll(p);
                return page.getContent();
            } else {
                return destinationRepository.findByUserId(userId,
                        new OffsetBasedPageRequest(offset, limit,
                                Sort.by(Source.DATE_CREATED).descending()));
            }
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<Destination>();
        }
    }


    public @NotNull Optional<Transaction> fetchTransactionByTransactionId(
            String transactionId) {
        try {
            logger.info(" {}", transactionId);
            return transactionRepository.findByTransactionId(transactionId);
        } catch (Exception e) {
            logger.error("", e);
            return Optional.empty();
        }
    }

    public @NotNull List<Transaction> fetchTransactionByPartnerIdAndUserId(UUID partnerID,
                                                                           UUID userId, Integer offset, Integer limit) {
        try {
            logger.info(" {} {} ", partnerID, userId);
            return transactionRepository.findByPartnerIdAndUserId(partnerID, userId, new OffsetBasedPageRequest(offset, limit,
                    Sort.by(Destination.DATE_CREATED).descending()));
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public @NotNull Optional<Transaction> fetchTransactionById(UUID id) {
        try {
            logger.info(" {}", id);
            return transactionRepository.findById(id);
        } catch (Exception e) {
            logger.error("", e);
            return Optional.empty();
        }
    }

    public Transaction fetchTransactionByReference(
            String reference, UUID userId) {
        try {
            logger.info(" {}", reference);

            List<Transaction> list = transactionRepository.findByReferenceAndPartnerId(reference, userId);

            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public List<Transaction> fetchTransactionByReference(
            String reference) {
        try {
            logger.info(" {}", reference);

            List<Transaction> list = transactionRepository.findByReference(reference);

            return list;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }



    public @NotNull List<Transaction> findByProcessedNullTransactionId() {
        try {
            logger.info("findByProcessedNullTransactionId ");
            List<Transaction> l = transactionRepository.findByTransactionStatusNotAndTransactionIdIsNull(TransactionStatus.CREATED);
            return l;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public @NotNull List<Transaction> fetchSourcedFailedUnreversedTransactions(
            LocalDateTime midnight) {
        try {
            logger.info("fetchFailedSourcedTransactions");

            return transactionRepository
                    .findSourcedFailedUnreversedTransactions(midnight);
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public @NotNull List<Transaction> fetchProcessingTransactions(LocalDateTime from) {
        try {
            logger.info("fetchProcessingTransactions {}",from);

            return transactionRepository.findByStatusBefore(TransactionStatus.PROCESSING, from);
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public @NotNull List<Transaction> fetchPendingReversal(LocalDateTime from) {
        try {
            logger.info("fetchPendingReversal {}",from);

            return transactionRepository.findByStatusAndTypeBefore(TransactionStatus.CREATED,
                TransactionType.REVERSAL, from);
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList<>();
        }
    }

    public void saveTransaction(Transaction transaction) {
        try {
            logger.info("saveTransaction {}", transaction.getId());
            transactionRepository.save(transaction);
        } catch (Exception e) {
            logger.error("", e);

        }
    }


}


