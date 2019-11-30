package net.tospay.transaction.controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.Transfer;
import net.tospay.transaction.models.request.Account;
import net.tospay.transaction.models.request.Amount;
import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.request.ChargeRequestDestination;
import net.tospay.transaction.models.request.ChargeRequestSource;
import net.tospay.transaction.models.request.TransferRequest;
import net.tospay.transaction.models.response.BaseResponse;
import net.tospay.transaction.models.response.ChargeResponse;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransferIncomingResponse;
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
            @Valid @RequestBody TransferRequest request) throws Exception
    {//Map<String, Object> allParams)//(@RequestBody TransactionGenericRequest request)
        // TopupRequest topupRequest = mapper.convertValue(allParams, TopupRequest.class);
        logger.info(" {}", request);
        return process(request);
    }

    public ResponseObject<BaseResponse> process(TransferRequest request)
            throws Exception
    {
        AtomicReference<Double> sumSourceAmount = new AtomicReference<>(0.0);
        request.getSource().forEach((topupValue) -> {
            sumSourceAmount.updateAndGet(v -> v + topupValue.getAmount());
        });

        if (!sumSourceAmount.get().equals(request.getAmount())) {
            logger.debug("destination amount and source don't tally {} {}", sumSourceAmount.get(),
                    request.getAmount());
            return new ResponseObject(ResponseCode.FAILURE.type, ResponseCode.FAILURE.name(),
                    Arrays.asList(new Error(ResponseCode.FAILURE.type,
                            String.format("destination amount and source don't tally %s %s", sumSourceAmount.get(),
                                    request.getAmount()))), request);
        }

        //create transaction
        //create source & Destination

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setMerchantId(request.getMerchantInfo().getUserId());
        //  JsonNode node = mapper.valueToTree(request);
        transaction.setPayload(request);
        transaction.setTransactionStatus(Transfer.TransactionStatus.CREATED);
        transaction.setTransactionType(request.getType());
        transaction.setExternalReference(request.getExternalReference());
        List<net.tospay.transaction.entities.Source> sources = new ArrayList<>();
        List<Double> destinationCharges = new ArrayList<>();
        request.getSource().forEach((topupValue) -> {

            net.tospay.transaction.entities.Source sourceEntity = new net.tospay.transaction.entities.Source();
            sourceEntity.setTransaction(transaction);
            sourceEntity.setAccount(topupValue.getAccount() != null ? topupValue.getAccount() : new Account());
          //  sourceEntity.getAccount().setEmail(request.getUserInfo() != null ? request.getUserInfo().getEmail() : null);
         //   sourceEntity.getAccount().setName(request.getUserInfo() != null ? request.getUserInfo().getName() : null);
            sourceEntity.setAmount(topupValue.getAmount());

            sourceEntity.setCurrency(transaction.getCurrency());
            sourceEntity.setTransactionStatus(transaction.getTransactionStatus());
            sourceEntity.setType(topupValue.getType());
            sourceEntity.setUserId(topupValue.getUserId());
            sourceEntity.setUserType(topupValue.getUserType());

            ChargeRequest chargeRequest = new ChargeRequest();
            chargeRequest.setType(transaction.getTransactionType());

            ChargeRequestDestination chargeRequestDestination =
                    new ChargeRequestDestination();
            //TODO: fix this for multiple - whose billed? when?
            String destAccountId = request.getDelivery().get(0).getAccount() != null ?
                    request.getDelivery().get(0).getAccount().getId() : null;
            chargeRequestDestination.setAccount(request.getDelivery().get(0).getUserType());//tospay account

            chargeRequestDestination.setId(destAccountId);
            chargeRequestDestination.setPlatform(destAccountId != null ? "known" : "unknown");
            chargeRequestDestination.setChannel(request.getDelivery().get(0).getType());
            chargeRequest.setDestination(chargeRequestDestination);

            ChargeRequestSource chargeRequestSource = new ChargeRequestSource();
            String sourceAccountId = sourceEntity.getAccount() != null ? sourceEntity.getAccount().getId() : null;

            chargeRequestSource.setAccount(sourceEntity.getUserType());//tospay account
            chargeRequestSource.setId(sourceAccountId);
            chargeRequestSource.setPlatform(sourceAccountId != null ? "known" : "unknown");
            chargeRequestSource.setChannel(sourceEntity.getType());
            chargeRequest.setSource(chargeRequestSource);

            Amount amount = new Amount();
            amount.setAmount(request.getAmount());
            amount.setCurrency(request.getCurrency());
            chargeRequest.setAmount(amount);

            ResponseObject<ChargeResponse> chargeResponse = fundService.fetchCharge(chargeRequest);

            if (chargeResponse != null && ResponseCode.SUCCESS.type.equalsIgnoreCase(chargeResponse.getStatus())) {
                Double sourceCharge = chargeResponse.getData().getSource() != null ?
                        chargeResponse.getData().getSource().getAmount().getAmount() : 0.0;
                Double destinationCharge = chargeResponse.getData().getSource() != null ?
                        chargeResponse.getData().getDestination().getAmount().getAmount() : 0.0;
                logger.debug("sourceCharge  {} sourceCharge  {}", sourceCharge, destinationCharge);

                destinationCharges.add(destinationCharge);

                sourceEntity.setCharge(sourceCharge);
            }

            transaction.addSource(sourceEntity);
            sources.add(sourceEntity);
        });

        request.getDelivery().forEach((topupValue) -> {
            net.tospay.transaction.entities.Destination destinationEntity =
                    new net.tospay.transaction.entities.Destination();
            destinationEntity.setTransaction(transaction);

            destinationEntity.setAmount(topupValue.getAmount());
            destinationEntity.setCurrency(transaction.getCurrency());
            destinationEntity.setTransactionStatus(transaction.getTransactionStatus());
            destinationEntity.setType(topupValue.getType());
            destinationEntity.setUserId(topupValue.getUserId());
            destinationEntity.setUserType(topupValue.getUserType());
            destinationEntity.setAccount(topupValue.getAccount());
//            if(topupValue.getAccount()==null) {
//                Account account = new Account();
//                account.setUserId(String.valueOf(topupValue.getUserId()));
//                account.setUserType(String.valueOf(topupValue.getUserType()));
//                ResponseObject<UserInfo> r = authService.getUserInfo(account);
//                if (r != null && ResponseCode.SUCCESS.type.equalsIgnoreCase(r.getStatus())) {
//                    destinationEntity.getAccount().setEmail(r.getData().getEmail());
//                    destinationEntity.getAccount().setName(r.getData().getName());
//                    destinationEntity.getAccount().setPhone(r.getData().getPhone());
//                }
//                destinationEntity.setAccount(account);
//            }

            //TODO: MULTIPLE RECIPIENT BILLING
            Double destinationCharge = destinationCharges.get(0);
            destinationEntity.setCharge(destinationCharge);
            destinationEntity.setAmount(sumSourceAmount.get());
            transaction.addDestination(destinationEntity);
        });

        transactionRepository.save(transaction);

        //trigger sourcing async
        CompletableFuture<Boolean> future = fundService.sourcePay(transaction);

        return new ResponseObject(ResponseCode.PROCESSING.type, ResponseCode.PROCESSING.name(), null,
                transaction.getId()
                        + " - " + transaction.getSources().get(0).getId()
                        + " - " + transaction.getDestinations().get(0).getId());//return mapResponse(response);
    }

    @PostMapping(Constants.URL.CALLBACK_MOBILE)
    public ResponseObject<BaseResponse> processCallback(
            @RequestBody
                    ResponseObject<TransferIncomingResponse> response)//(@RequestBody TransactionGenericRequest request)
            throws Exception
    {
        Map node = mapper.convertValue(response, Map.class);
        logger.debug(" {}", node);

        Optional<net.tospay.transaction.entities.Source> optionalSource =
                response.getData() == null ? Optional.empty() :
                        sourceRepository.findById(response.getData().getExternalReference());
        Optional<net.tospay.transaction.entities.Destination> optionalDestination =
                response.getData() == null ? Optional.empty() :
                        destinationRepository.findById(response.getData().getExternalReference());

        Transaction transaction = optionalSource.isPresent() ? optionalSource.get().getTransaction() :
                (optionalDestination.isPresent() ? optionalDestination.get().getTransaction() : null);

        Transfer.TransactionStatus transactionStatus =
                ResponseCode.SUCCESS.type.equalsIgnoreCase(response.getStatus()) ? Transfer.TransactionStatus.SUCCESS :
                        Transfer.TransactionStatus.FAILED;
        if (optionalSource.isPresent()) {
            net.tospay.transaction.entities.Source source = optionalSource.get();
            source.setResponseAsync(node);
            source.setDateResponse(new Timestamp(System.currentTimeMillis()));
            source.setTransactionStatus(transactionStatus);
            source = sourceRepository.save(source);
            transaction = source.getTransaction();
            if (Transfer.TransactionStatus.FAILED.equals(transactionStatus)) {
                logger.debug("sourcing failed  {}", source);
            }
        } else if (optionalDestination.isPresent()) {
            net.tospay.transaction.entities.Destination destination = optionalDestination.get();
            destination.setResponseAsync(node);
            destination.setDateResponse(new Timestamp(System.currentTimeMillis()));
            destination.setTransactionStatus(transactionStatus);
            destination = destinationRepository.save(destination);
            transaction = destination.getTransaction();
            if (Transfer.TransactionStatus.FAILED.equals(transactionStatus)) {
                logger.debug("paydestination failed  {}", destination);
            }
        } else {
            //TODO: callback from where?
            logger.error("callback called but no transaction found {}", node);
        }

        // transaction = transactionRepository.save(transaction);//transactionRepository.refresh(transaction);

        fundService.checkSourceAndDestinationTransactionStatusAndAct(transaction);

        String status = ResponseCode.SUCCESS.type;
        String description = ResponseCode.SUCCESS.name();
        return new ResponseObject(status, description, null, response.getData().getExternalReference());
    }
}
