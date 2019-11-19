package net.tospay.transaction.services;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.MobilePayAction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.request.TopupMobileRequest;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TopupMobileResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;

@Service
public class FundService extends BaseService
{
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Autowired TransactionRepository transactionRepository;

    @Autowired SourceRepository sourceRepository;

    @Autowired DestinationRepository destinationRepository;

    @Value("${mobilepay.url}")
    private String mobilepayUrl;

    public FundService()
    {

    }

    public FundService(RestTemplate restTemplate, TransactionRepository transactionRepository,
            SourceRepository sourceRepository, DestinationRepository destinationRepository)
    {
        this.restTemplate = restTemplate;

        this.transactionRepository = transactionRepository;

        this.sourceRepository = sourceRepository;

        this.destinationRepository = destinationRepository;
    }

    @Async
    public CompletableFuture<Boolean> paySource(List<Source> sources)
    {
        try {

            sources.forEach(source -> {

                switch (source.getType()) {
                    case MOBILE: //async

                        source.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(source.getAccount());
                        request.setAction(MobilePayAction.SOURCE);
                        request.setAmount(source.getAmount());
                        request.setUserId(source.getUserId());
                        request.setUserType(source.getUserType());
                        request.setCharge(source.getCharge());
                        request.setCurrency(source.getCurrency());
                        request.setDescription(source.getType().toString());
                        request.setExternalReference(source.getId());

                        ResponseObject<TopupMobileResponse> response = pay(request);

                        //  JsonNode node = mapper.valueToTree(request);
                        if (response != null) {
                            source.setResponse(response.getData());
                        }
                        if (response == null || !ResponseCode.SUCCESS.code
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            source.setTransactionStatus(TransactionStatus.FAIL);
                            source.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        } else {
                            logger.debug("sourcing ok", source);
                        }

                        break;
                    case BANK:
                        break;
                    case WALLET: //sync

                        break;
                    case CARD:
                        break;
                }

                sourceRepository.save(source);
            });

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("", e);
            return CompletableFuture.completedFuture(false);
        }
    }

    public ResponseObject<TopupMobileResponse> pay(TopupMobileRequest request)
    {
        try {
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TopupMobileRequest>(request, headers);

            ResponseObject<TopupMobileResponse> response =
                    restTemplate.postForObject(mobilepayUrl, entity, ResponseObject.class);
            logger.debug("", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error("", e.getResponseBodyAsString());

            return null;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    @Async
    public CompletableFuture<Boolean> payDestination(List<Destination> destinations)
    {
        try {

            destinations.forEach(destination -> {

                switch (destination.getType()) {
                    case MOBILE: { //async wait for callback

                        destination.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(destination.getAccount());
                        request.setAction(MobilePayAction.DESTINATION);
                        request.setAmount(destination.getAmount());
                        request.setUserId(destination.getUserId());
                        request.setUserType(destination.getUserType());
                        request.setCharge(destination.getCharge());
                        request.setCurrency(destination.getCurrency());
                        request.setDescription(destination.getType().toString());
                        request.setExternalReference(destination.getId());

                        ResponseObject<TopupMobileResponse> response = pay(request);

                        //  JsonNode node = mapper.valueToTree(request);
                        destination.setResponse(response.getData());
                        if (response == null || !ResponseCode.SUCCESS.code
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            destination.setTransactionStatus(TransactionStatus.FAIL);
                            destination.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        } else {

                        }

                        break;
                    }
                    case BANK:
                        break;
                    case WALLET: { //sync

                        destination.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(destination.getAccount());
                        request.setAction(MobilePayAction.DESTINATION);
                        request.setAmount(destination.getAmount());
                        request.setUserId(destination.getUserId());
                        request.setUserType(destination.getUserType());
                        request.setCharge(destination.getCharge());
                        request.setCurrency(destination.getCurrency());
                        request.setDescription(destination.getType().toString());
                        request.setExternalReference(destination.getId());

                        ResponseObject<TopupMobileResponse> response = pay(request);

                        //  JsonNode node = mapper.valueToTree(request);
                        destination.setResponse(response.getData());
                        if (response == null || !ResponseCode.SUCCESS.code
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            destination.setTransactionStatus(TransactionStatus.FAIL);
                            destination.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        } else { //success

                        }

                        break;
                    }
                    case CARD:
                        break;
                }

                destinationRepository.save(destination);
            });

            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            logger.error("", e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
