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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.MobilePayAction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.request.TopupMobileRequest;
import net.tospay.transaction.models.response.BaseResponse;
import net.tospay.transaction.models.response.ResponseObject;
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
    public CompletableFuture<Boolean> pay(List<Source> sources)
    {
        try {

            sources.forEach(source -> {

                switch (source.getType()) {
                    case MOBILE:

                        source.setTransactionStatus(TransactionStatus.PROCESSING);
                        TopupMobileRequest request = new TopupMobileRequest();
                        request.setAccount(source.getAccount());
                        request.setAction(MobilePayAction.SOURCE);
                        request.setAmount();
                        request.setUserId();
                        request.setUserType();
                        request.setCharge();
                        request.setCurrency();
                        request.setDescription();
                        request.setExternalReference();

                        ResponseObject<BaseResponse> response = pay(request);

                        JsonNode node = mapper.valueToTree(request);
                        source.setResponse(node);
                        if (response == null || !ResponseCode.SUCCESS.code
                                .equalsIgnoreCase(response.getStatus()))
                        {//failure
                            source.setTransactionStatus(TransactionStatus.FAIL);
                            source.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        } else {
                        }

                        break;
                    case BANK:
                        break;
                    case WALLET:
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

    public ResponseObject<BaseResponse> pay(TopupMobileRequest request)
    {
        try {
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TopupMobileRequest>(request, headers);

            ResponseObject<BaseResponse> response =
                    restTemplate.postForObject(mobilepayUrl, entity, ResponseObject.class);
            logger.debug("", response);

            return response;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }
}
