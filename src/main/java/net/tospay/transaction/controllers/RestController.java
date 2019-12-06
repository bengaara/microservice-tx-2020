package net.tospay.transaction.controllers;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.response.BaseResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.repositories.DestinationRepository;
import net.tospay.transaction.repositories.SourceRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.AuthService;
import net.tospay.transaction.services.FundService;
import net.tospay.transaction.util.Constants;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API + "/v1")
public class RestController extends BaseController
{
    net.tospay.transaction.services.FundService fundService;

    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Autowired TransactionRepository transactionRepository;

    @Autowired SourceRepository sourceRepository;

    @Autowired DestinationRepository destinationRepository;

    AuthService authService;

    public RestController(FundService fundService, AuthService authService)
    {
        this.fundService = fundService;
        this.authService = authService;
    }

    @PostMapping(Constants.URL.TRANSFER)
    public ResponseObject<BaseResponse> transfer(
            @Valid @RequestBody TransactionRequest request) throws Exception
    {//Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
        // TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
        logger.info(" {}", request);
        return process(request);
    }

    public ResponseObject<BaseResponse> process(TransactionRequest request)
            throws Exception
    {

        AtomicReference<BigDecimal> sumSourceAmount = new AtomicReference<>(BigDecimal.ZERO);
        request.getSource().forEach((topupValue) -> {
            sumSourceAmount.updateAndGet(v -> v.add(topupValue.getTotal().getAmount()));
        });

        if (!sumSourceAmount.get().equals(request.getOrderInfo().getAmount().getAmount())) {
            logger.debug("source amount and totals don't tally {} {}", sumSourceAmount.get(),
                    request.getOrderInfo().getAmount().getAmount());
            return new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.FAILURE.type,
                            String.format("destination amount and source don't tally %s %s", sumSourceAmount.get(),
                                    request.getOrderInfo().getAmount()))), request);
        }

        AtomicReference<BigDecimal> destSourceAmount = new AtomicReference<>(BigDecimal.ZERO);
        request.getDelivery().forEach((topupValue) -> {
            destSourceAmount.updateAndGet(v -> v.add(topupValue.getTotal().getAmount()));
        });
        BigDecimal chargetotal = request.getChargeInfo().getDestination().getAmount();
        chargetotal.add(request.getChargeInfo().getFx().getAmount().getAmount());
        chargetotal.add(request.getChargeInfo().getPartnerInfo().getAmount().getAmount());
        chargetotal.add(request.getChargeInfo().getRailInfo().getAmount().getAmount());

        if (!request.getOrderInfo().getAmount().getAmount().equals(destSourceAmount.get().add(chargetotal))) {
            logger.debug("destination amount and source don't tally {} {}", destSourceAmount.get(),
                    request.getOrderInfo().getAmount());
            return new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.FAILURE.type,
                            String.format("destination amount and source don't tally %s %s", destSourceAmount.get(),
                                    request.getOrderInfo().getAmount()))), request);
        }

        //create transaction
        //create source & Destination

        net.tospay.transaction.entities.Transaction transaction = new net.tospay.transaction.entities.Transaction();
        //  JsonNode node = mapper.valueToTree(request);

        transaction.setPayload(request);
        transaction.setTransactionStatus(TransactionStatus.CREATED);

        request.getSource().forEach((topupValue) -> {

            net.tospay.transaction.entities.Source sourceEntity = new net.tospay.transaction.entities.Source();
            sourceEntity.setTransaction(transaction);
            sourceEntity.setPayload(topupValue);
            sourceEntity.setTransactionStatus(transaction.getTransactionStatus());

            transaction.addSource(sourceEntity);
        });

        request.getDelivery().forEach((topupValue) -> {
            net.tospay.transaction.entities.Destination destinationEntity =
                    new net.tospay.transaction.entities.Destination();
            destinationEntity.setTransaction(transaction);

            destinationEntity.setTransactionStatus(transaction.getTransactionStatus());

            transaction.addDestination(destinationEntity);
        });

        transactionRepository.saveAndFlush(transaction);

        //trigger sourcing async
        CompletableFuture<Boolean> future = fundService.sourcePay(transaction);

        return new ResponseObject(ResponseCode.PROCESSING.type, ResponseCode.PROCESSING.name(), null,
                transaction.getId()
                        + " - " + transaction.getSources().get(0).getId()
                        + " - " + transaction.getDestinations().get(0).getId());//return mapResponse(response);
    }

    @PostMapping(Constants.URL.CALLBACK_MOBILE)
    public ResponseObject<BaseResponse> processCallback(@RequestBody ResponseObject<StoreResponse> response)
    {
        Map node = mapper.convertValue(response, Map.class);
        logger.debug(" {}", node);

        fundService.processPaymentCallback(response.getData());

        String status = ResponseCode.SUCCESS.type;
        String description = ResponseCode.SUCCESS.name();
        return new ResponseObject(status, description, null, response.getData().getExternalReference());
    }
}
