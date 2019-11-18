package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.SourceType;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.models.request.Amount;
import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.request.Source;
import net.tospay.transaction.models.request.TopupRequest;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.response.BaseResponse;
import net.tospay.transaction.models.response.ChargeResponse;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TopupMobileResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.ChargeRestService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.util.Constants;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API + "/v1")
public class RestController extends BaseController
{
    ChargeRestService chargeRestService;

    net.tospay.transaction.services.FundService fundService;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Autowired TransactionRepository transactionRepository;

    @Autowired SourceRepository sourceRepository;

    @Autowired DestinationRepository destinationRepository;

    @Value("${numbergenerator.transaction.url}")
    String numberGeneratorTransactionUrl;

    @Value("${charge.url}")
    String chargeUrl;

    public RestController(ChargeRestService coopRestService, FundService fundService)
    {
        this.chargeRestService = coopRestService;
        this.fundService = fundService;
    }

    @PostMapping(Constants.URL.PROCESS)
    public ResponseObject<BaseResponse> process(
            @RequestParam Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
            throws Exception
    {
        //@RequestParam Map<String,String> allParams
        //validate
        //get transaction type
        //call corresponding method
        //wait for callback
        TransactionType transactionType =
                allParams.containsKey("type") ? TransactionType.valueOfType(allParams.get("type").toString()) : null;

        // String jsonStr = Obj.writeValueAsString(request);
        switch (transactionType) {
            case TOPUP:
//                JavaType type = mapper.getTypeFactory().constructParametricType(Map.class, TopupRequest.class);
//                TypeReference<TopupRequest> typeRef
//                        = new TypeReference<TopupRequest>()
//                {
//                };
                TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
                return process(topupRequest);
            break;
            case WITHDRAW:
                break;
            case TRANSFER:
                break;
            case PAYMENT:
                break;
            case INVOICE:
                break;
            default:
                break;
        }
    }

    public ResponseObject<BaseResponse> process(TopupRequest request)
            throws Exception
    {
        //TODO: validate

        //create transaction
        //create source & Destination

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setMerchantId(request.getMerchantInfo());
        JsonNode node = mapper.valueToTree(request);
        transaction.setPayload(node);
        transaction.setTransactionStatus(TransactionStatus.CREATED);
        transaction.setTransactionType(TransactionType.TOPUP);

        transactionRepository.save(transaction);

        List<net.tospay.transaction.entities.Source> sources = new ArrayList<>();
        request.getSources().forEach((topupValue) -> {

            net.tospay.transaction.entities.Source sourceEntity = new net.tospay.transaction.entities.Source();
            sourceEntity.setTransaction(transaction);
            sourceEntity.setAccount(topupValue.getAccount());
            sourceEntity.setAmount(transaction.getAmount());
            sourceEntity.setCurrency(transaction.getCurrency());
            sourceEntity.setTransactionStatus(transaction.getTransactionStatus());
            sourceEntity.setType(topupValue.getType());
            sourceEntity.setUserId(topupValue.getUserId());
            sourceEntity.setUserType(topupValue.getUserType());

            net.tospay.transaction.entities.Destination destinationEntity =
                    new net.tospay.transaction.entities.Destination();
            destinationEntity.setTransaction(transaction);
            destinationEntity.setAccount(request.getDelivery().get(0).getAccount());
            destinationEntity.setAmount(transaction.getAmount());

            destinationEntity.setCurrency(transaction.getCurrency());
            destinationEntity.setTransactionStatus(transaction.getTransactionStatus());
            destinationEntity.setType(request.getDelivery().get(0).getType());
            destinationEntity.setUserId(request.getDelivery().get(0).getUserId());
            destinationEntity.setUserType(request.getDelivery().get(0).getUserType());

            ChargeRequest chargeRequest = new ChargeRequest();

            net.tospay.transaction.models.request.Destination destinationRequest =
                    new net.tospay.transaction.models.request.Destination();
            destinationRequest.setAccount(destinationEntity.getAccount());//tospay account
            destinationRequest.setId(request.getDelivery().get(0).getAccount());
            destinationRequest.setChannel(request.getDelivery().get(0).getType());
            chargeRequest.setDestination(destinationRequest);
            Source source = new Source();
            source.setAccount(topupValue.getAccount());
            source.setId(topupValue.getAccount());
            source.setChannel(topupValue.getType());
            chargeRequest.setSource(source);
            Amount amount = new Amount();
            amount.setAmount(request.getAmount());
            amount.setCurrency(request.getCurrency());
            chargeRequest.setAmount(amount);
            chargeRequest.setType(TransactionType.TOPUP);

            // ChargeResponse chargeResponse = chargeRestService.fetchCharge(chargeRequest);
            ResponseObject<ChargeResponse> chargeResponse = fetchCharge(chargeRequest);

            if (chargeResponse != null && ResponseCode.SUCCESS.code.equalsIgnoreCase(chargeResponse.getStatus())) {
                sourceEntity.setCharge(
                        chargeResponse != null ? chargeResponse.getData().getSource().getAmount().getAmount() : null);
                destinationEntity
                        .setCharge(chargeResponse != null ?
                                chargeResponse.getData().getDestination().getAmount().getAmount() : null);
            }

            sourceRepository.save(sourceEntity);
            destinationRepository.save(destinationEntity);
            sources.add(sourceEntity);
        });

        //trigger sourcing async
        CompletableFuture<Boolean> future = fundService.pay(sources);

        //transaction.setTransactionId();//TODO set this on success

        return new ResponseObject(ResponseCode.SUCCESS, ResponseCode.SUCCESS.name(), null,
                null);//return mapResponse(response);
    }

    @PostMapping(Constants.URL.PROCESS_CALLBACK)
    public ResponseObject<BaseResponse> processCallback(
            @RequestParam Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
            throws Exception
    {

        logger.debug("", allParams);
        SourceType sourceType =
                allParams.containsKey("channel") ?
                        SourceType.valueOfType(allParams.get("channel").toString()) : null;

        // String jsonStr = Obj.writeValueAsString(request);
        switch (sourceType) {
            case MOBILE:
                ResponseObject<TopupMobileResponse> response = mapper.convertValue(allParams, ResponseObject.class);

                Optional<net.tospay.transaction.entities.Source> optionalSource =
                        sourceRepository.fetchDistinctById(response.getData().getExternalReference());
                Optional<net.tospay.transaction.entities.Destination> optionalDestination =
                        destinationRepository.fetchDistinctById(response.getData().getExternalReference());

                if (!ResponseCode.SUCCESS.code
                        .equalsIgnoreCase(response.getStatus()))
                {//failure

                    if (optionalSource.isPresent()) {
                        net.tospay.transaction.entities.Source source = optionalSource.get();
                        source.setTransactionStatus(TransactionStatus.FAIL);
                        source.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        sourceRepository.save(source);
                    } else if (optionalDestination.isPresent()) {
                        net.tospay.transaction.entities.Destination destination = optionalDestination.get();
                        destination.setTransactionStatus(TransactionStatus.FAIL);
                        destination.getTransaction().setTransactionStatus(TransactionStatus.FAIL);
                        destinationRepository.save(destination);
                    } else {
                        //TODO: callback from where?
                        logger.error("", "callback called but no transaction found", allParams);
                    }
                } else { //success
                    if (optionalSource.isPresent()) {
                        net.tospay.transaction.entities.Source source = optionalSource.get();
                        source.setTransactionStatus(TransactionStatus.SUCCESS);
                        sourceRepository.save(source);

                        //if all success - mark transaction source complete and fire deposit
                        
                    } else if (optionalDestination.isPresent()) {
                        net.tospay.transaction.entities.Destination destination = optionalDestination.get();
                        destination.setTransactionStatus(TransactionStatus.FAIL);
                        destinationRepository.save(destination);
                        //if all success - mark transaction source complete and fire deposit

                    } else {
                        //TODO: callback from where?
                        logger.error("", "callback called but no transaction found", allParams);
                    }
                }

                break;
            case BANK:
                break;
            case CARD:
                break;
            case WALLET:
                break;
            default:
                break;
        }
    }

    public ResponseObject<ChargeResponse> fetchCharge(ChargeRequest chargeRequest)
    {
        try {
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ChargeRequest>(chargeRequest, headers);

            ResponseObject<ChargeResponse> response =
                    restTemplate.postForObject(chargeUrl, entity, ResponseObject.class);
            logger.debug("", response);

            return response;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public ResponseObject<String> fetchTransactionId(AccountType accountType, TransactionType transactionType,
            String countryCode)
    {
        try {

            TransactionIdRequest request = new TransactionIdRequest();
            request.setAccountType(accountType);
            request.setCountry(countryCode);
            request.setTransactionType(transactionType);

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransactionIdRequest>(request, headers);

            ResponseObject<String> response =
                    restTemplate.postForObject(numberGeneratorTransactionUrl, entity, ResponseObject.class);
            logger.debug("", response);

            return response;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }
}
