package net.tospay.transaction.services;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class NotifyService extends BaseService
{
    RestTemplate restTemplate;

    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    @Value("${notify.transfer.url}")
    String notifyTransferUrl;

    public NotifyService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository)
    {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    public void notifyTransferDestination(List<Destination> list)
    {
        try {

            list.forEach(d -> {

                        if (d.getUserId() == null) {
                            logger.debug("notifyTransferDestination failed - no userId {}", d);

                        }else {
                            NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
                            request.setCategory(Notify.Category.TRANSFER);
                            request.setTopic(d.getTransaction().getTransactionType());
                            request.setStatus(d.getTransactionStatus());
                            request.setAmount(d.getAmount().toString());
                            request.setCurrency(d.getCurrency());
                            request.setRecipientId( String.valueOf(d.getUserId()));
                            request.setRecipientType(d.getUserType().toString());

                            NotifyTransferOutgoingRequest response = hitNotify(request);
                        }
                    }

            );
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
    }

    public void notifyTransferSource(List<Source> list)
    {
        try {

            list.forEach(d -> {
                if (d.getUserId() == null) {
                    logger.debug("notifyTransferDestination failed - no userId {}", d);

                }else {
                    NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
                    request.setCategory(Notify.Category.TRANSFER);
                    request.setTopic(d.getTransaction().getTransactionType());
                    request.setStatus(d.getTransactionStatus());
                    request.setAmount(d.getAmount().toString());
                    request.setCurrency(d.getCurrency());
                    request.setRecipientId( String.valueOf(d.getUserId()));
                    request.setRecipientType(d.getUserType().toString());

                    NotifyTransferOutgoingRequest response = hitNotify(request);
                }
            }


            );
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
    }

    NotifyTransferOutgoingRequest hitNotify(NotifyTransferOutgoingRequest request)
    {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<NotifyTransferOutgoingRequest>(request, headers);

            logger.debug("sending notify{}", request);
            NotifyTransferOutgoingRequest response =
                    restTemplate.postForObject(notifyTransferUrl, entity, NotifyTransferOutgoingRequest.class);
            logger.debug("{}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error("{}", e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }
}
