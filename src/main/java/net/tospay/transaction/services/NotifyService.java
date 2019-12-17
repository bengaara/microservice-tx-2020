package net.tospay.transaction.services;

import java.time.LocalDateTime;
import java.time.ZoneId;
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

import net.tospay.transaction.entities.BaseSource;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.models.request.NotifyTransferOutgoingSenderRequest;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.util.Utils;

@Service
public class NotifyService extends BaseService
{
    RestTemplate restTemplate;

    TransactionRepository transactionRepository;

    SourceRepository sourceRepository;

    DestinationRepository destinationRepository;

    @Value("${notify.transfer.url}")
    String notifyTransferUrl;

    DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("d MMM yyyy h:mm a");

    public NotifyService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository)
    {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    public void notifyDestination(Transaction transaction)
    {
        try {
            List<Destination> list = transaction.getDestinations();
            list.stream().filter(destination -> {
                return destination.getTransactionStatus().equals(TransactionStatus.SUCCESS) ||
                        destination.getTransactionStatus().equals(TransactionStatus.FAILED);
            }).forEach(d -> {

                logger.debug("notifyDestination {}", d.getId());
                notifyGateway(d, "credit");
            });
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
    }

    public void notifySource(Transaction transaction)
    {
        try {

            List<Source> list = transaction.getSources();
            list.stream().filter(source -> {
                return source.getTransactionStatus().equals(TransactionStatus.SUCCESS) ||
                        source.getTransactionStatus().equals(TransactionStatus.FAILED);
            }).forEach(d -> {

                        logger.debug("notifySource {}", d.getId());
                        notifyGateway(d, "debit");
                    }
            );
        } catch (Exception e) {
            logger.error("", e);
            return;
        }
    }

    void notifyGateway(BaseSource entity, String operation)
    {
        logger.debug("notifySource {}", entity.getId());

        NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
        request.setTopic(entity.getTransaction().getType());
        request.setStatus(entity.getTransactionStatus());
        request.setAmount(entity.getPayload().getTotal().getAmount());
        request.setCurrency(entity.getPayload().getTotal().getCurrency());
        request.setRecipientId(String.valueOf(entity.getPayload().getAccount().getUserId()));
        request.setRecipientType(String.valueOf(entity.getPayload().getAccount().getUserType()));
        request.setReference(entity.getTransaction().getTransactionId());
        request.setDate(Utils.FORMATTER.format(LocalDateTime.now()));
        request.setOperation(operation);
//        if(entity.getPayload().getAccount().getCountry() !=null){
//            final List<String> timeZones = Stream.of(TimeZone.getAvailableIDs())
//                    .filter(zoneId -> zoneId.startsWith(entity.getPayload().getAccount().getCountry().getIso())).collect(Collectors.toList());
//          String date =  Utils.FORMATTER.format(ZonedDateTime.now().withZoneSameInstant(ZoneId.of(timeZones.get(0))) .toLocalDateTime());
//            request.setDate(date);
//
//        }
        request.setDate(Utils.FORMATTER.format(LocalDateTime.now().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("Africa/Nairobi")).toLocalDateTime()));

        List<NotifyTransferOutgoingSenderRequest> list1 = new ArrayList<>();
        entity.getTransaction().getSources().forEach(ds -> {

            NotifyTransferOutgoingSenderRequest sender =
                    new NotifyTransferOutgoingSenderRequest(
                            String.valueOf(ds.getPayload().getAccount().getUserId()),
                            String.valueOf(ds.getPayload().getAccount().getUserType()));
//                                    sender.setSenderName(s.getAccount()!=null?s.getAccount().getName():null);
//                                    sender.setSenderEmail(s.getAccount()!=null?s.getAccount().getEmail():null);
            list1.add(sender);
        });
        request.setSenders(list1.size() > 0 ? list1 : null);
        List<NotifyTransferOutgoingSenderRequest> list2 = new ArrayList<>();
        entity.getTransaction().getDestinations().forEach(ds -> {

            NotifyTransferOutgoingSenderRequest receiver =
                    new NotifyTransferOutgoingSenderRequest();
            receiver.setReceiverId(String.valueOf(ds.getPayload().getAccount().getUserId()));
            receiver.setReceiverType(String.valueOf(ds.getPayload().getAccount().getUserType()));
//                                    sender.setSenderName(s.getAccount()!=null?s.getAccount().getName():null);
//                                    sender.setSenderEmail(s.getAccount()!=null?s.getAccount().getEmail():null);
            list2.add(receiver);
        });
        request.setReceivers(list2.size() > 0 ? list2 : null);

        NotifyTransferOutgoingRequest response = hitNotify(request);
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
