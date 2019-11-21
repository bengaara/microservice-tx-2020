package net.tospay.transaction.services;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.models.request.NotifyTransferOutgoingSenderRequest;
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

            DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");
            list.forEach(d -> {

                        if (d.getUserId() == null) {
                            logger.debug("notifyTransferDestination failed - no userId {}", d.getId());

                        }else {
                            logger.debug("notifyTransferDestination", d.getId());
                            NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
                            // request.setCategory(Notify.Category.TRANSFER);
                            request.setTopic(d.getTransaction().getTransactionType());
                            request.setStatus(d.getTransactionStatus());
                            request.setAmount(d.getAmount().toString());
                            request.setCurrency(d.getCurrency());
                            request.setRecipientId( String.valueOf(d.getUserId()));
                            request.setRecipientType(String.valueOf(d.getUserType()));
                            request.setReference(d.getTransaction().getTransactionId());
                            request.setDate(FOMATTER.format(LocalDateTime.now()));

                            List<NotifyTransferOutgoingSenderRequest> l = new ArrayList<>();
                            d.getTransaction().getSources().forEach(s->{
                                if(s.getUserId() !=null) {
                                    NotifyTransferOutgoingSenderRequest sender =
                                            new NotifyTransferOutgoingSenderRequest(String.valueOf(s.getUserId()),
                                                    String.valueOf(s.getUserType()));
                                }
                            });
                            request.setSenders(l.size()>0?l:null);
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

            DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");
            list.forEach(d -> {
                if (d.getUserId() == null) {
                    logger.debug("notifyTransferSources failed - no userId {}", d.getId());

                }else {
                    logger.debug("notifyTransferSource", d.getId());
                    NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
                   // request.setCategory(Notify.Category.TRANSFER);
                    request.setTopic(d.getTransaction().getTransactionType());
                    request.setStatus(d.getTransactionStatus());
                    request.setAmount(d.getAmount().toString());
                    request.setCurrency(d.getCurrency());
                    request.setRecipientId( String.valueOf(d.getUserId()));
                    request.setRecipientType(String.valueOf(d.getUserType()));
                    request.setReference(d.getTransaction().getTransactionId());
                    request.setDate(FOMATTER.format(LocalDateTime.now()));

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

            logger.debug("sending notify{}", objectMapper.writeValueAsString(request));
            NotifyTransferOutgoingRequest response =
                    restTemplate.postForObject(notifyTransferUrl, entity, NotifyTransferOutgoingRequest.class);
            logger.debug("{}", objectMapper.writeValueAsString(response));

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
