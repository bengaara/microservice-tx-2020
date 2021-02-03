package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Reversal;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.FraudStatus;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.Store;
import net.tospay.transaction.models.TransactionRequest;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.WalletAccount;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class ReversalService extends BaseService {


    @Autowired //TODO bug dont remove - when method is called asyn they r needed
        TransactionRepository transactionRepository;

    @Value("${walletpay.account.url}")
    String walletAccountUrl;

    public ReversalService() {

    }


    public Transaction reverseTransaction(Transaction t, Reversal reversal) {

        if (TransactionType.REVERSAL.equals(t.getType()) || t.getReversalChild() != null) {
            logger
                .debug("no refund for transaction {} status {} type {} reversalchild {}", t.getId(),
                    t.getTransactionStatus(), t.getType(), t.getReversalChild());
            return null;
        }

        if (reversal == null) {//  automatic reversal
            if (!TransactionStatus.FAILED.equals(t.getTransactionStatus())) {
                logger.debug(
                    "no refund for transaction {} status {} type {} reversal {} reversalchild {}",
                    t.getId(),
                    t.getTransactionStatus(), t.getType(), reversal == null, t.getReversalChild());
                return null;
            }
        } else {//manual reversal - tx must b successful
            if (TransactionStatus.FAILED.equals(t.getTransactionStatus())) {
                logger.debug(
                    "no refund for transaction {} status {} type {} reversal {} reversalchild {}",
                    t.getId(),
                    t.getTransactionStatus(), t.getType(), reversal == null, t.getReversalChild());
                return null;
            }
        }


        if (!t.getSources().stream().anyMatch(source -> {
            return TransactionStatus.SUCCESS.equals(source.getTransactionStatus());
        })) {
            logger.debug("no refund for transaction. No complete source {} status {} type {} {}",
                t.getId(),
                t.getTransactionStatus(), t.getType());
            return null;
        }

        Transaction transaction = new Transaction();
        //  JsonNode node = mapper.valueToTree(request);
        transaction.setReversalParent(t);
        t.setReversalChild(transaction);
        t.setReversed(true);
        if(t.getTransactionId()!=null) {
            transaction.setTransactionId(t.getTransactionId() + "-R");
        }
        transaction.setUserInfo(t.getUserInfo());
        transaction.setPayload(t.getPayload());
        transaction.setTransactionStatus(TransactionStatus.CREATED);
        // transaction.setTransactionStatus(TransactionStatus.PROCESSING);
        transaction.setType(TransactionType.REVERSAL);

        if (reversal == null) {
            logger.debug("SYSTEM reversal {} ReverseCharge true {}");
            transaction.setReverseCharge(true);
        } else {
            logger.debug("reversal {} ", reversal);

            transaction.setReverseCharge(reversal.isReverseCharge());
            // transaction.setReversal(reversal);
            reversal.setReversalTransaction(transaction);
        }

        BigDecimal totalReversed = BigDecimal.ZERO;
        for (Source source : t.getSources()) {
            if (TransactionStatus.SUCCESS.equals(source.getTransactionStatus())) {
                Store payload = (Store) Utils.deepCopy(source.getPayload());

                //take cash to wallet if
                if (AccountType.CARD.equals(source.getPayload().getAccount().getType())) {
                    logger
                        .debug("reversal to card not supported.. reroute to wallet {} ", reversal);
                    ResponseObject<WalletAccount> res = this
                        .hitWalletAccountService(payload.getAccount().getUserId().toString(),
                            payload.getAccount().getUserType());
                    if (ResponseCode.SUCCESS.type.equalsIgnoreCase(res.getStatus())
                        && res.getData() != null) {
                        payload.getAccount().setId(res.getData().getId().toString());

                    }
                }
                if (!transaction.isReverseCharge()) {
                    Amount amount = new Amount(payload.getOrder());
                    Amount charge = new Amount(payload.getCharge()).withAmount(BigDecimal.ZERO);
                    payload.setCharge(charge);
                    payload.setTotal(amount);
                }
                if (reversal != null && reversal.getAmount() != null
                    && payload.getTotal().getAmount().compareTo(reversal.getAmount()) > 0) {
                    logger.debug("reverseAmount less  {} {} ", reversal.getAmount(),
                        payload.getTotal().getAmount());
                    payload.setTotal(payload.getTotal().withAmount(reversal.getAmount()));

                }

                totalReversed = totalReversed.add(payload.getTotal().getAmount());
                // destinationEntity.setTransaction(transaction);
                Destination destinationEntity = new Destination();
                destinationEntity.setPayload(payload);
                destinationEntity.setTransactionStatus(TransactionStatus.CREATED);
                destinationEntity.setTransaction(transaction);

                transaction.addDestination(destinationEntity);
            }
        }
        transaction.setReverseAmount(totalReversed);

        for (Destination destination : t.getDestinations()) {
            if (TransactionStatus.SUCCESS.equals(destination.getTransactionStatus())) {

                if (!transaction.isReverseCharge() && destination
                    .isRevenue()) { //not reversing charge.. so skip revenue
                    logger.debug("skip revenue reversal {} ReverseCharge false {}",
                        destination.getId());
                    continue;
                }
                Store payload = (Store) Utils.deepCopy(destination.getPayload());

                if (!transaction.isReverseCharge()) {
                    Amount amount = new Amount(payload.getOrder());
                    Amount charge = new Amount(payload.getCharge()).withAmount(BigDecimal.ZERO);
                    payload.setCharge(charge);
                    payload.setTotal(amount);
                }

                if (reversal != null && reversal.getAmount() != null
                    && payload.getTotal().getAmount().compareTo(reversal.getAmount()) > 0) {
                    logger.debug("reverseAmount less  {} {} ", reversal.getAmount(),
                        payload.getTotal().getAmount());
                    payload.setTotal(payload.getTotal().withAmount(reversal.getAmount()));
                }

                // sourceEntity.setTransaction(transaction);
                Source sourceEntity = new Source();
                sourceEntity.setPayload(payload);
                sourceEntity.setTransactionStatus(TransactionStatus.CREATED);
                sourceEntity.setTransaction(transaction);
                transaction.addSource(sourceEntity);
            }
        }
        transactionRepository.save(transaction);
        transactionRepository.save(t);
        return transaction;
    }


    public ResponseObject<WalletAccount> hitWalletAccountService(String id, UserType userType) {
        try {
            String url = walletAccountUrl.replace("{id}", id).replace("{type}", userType.name());
            this.logger.debug("hitTransactionLimitService request: {} {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ForexObject>(null, headers);

            ResponseEntity<ResponseObject<WalletAccount>> response = restTemplate
                .exchange(url, HttpMethod.GET, entity,
                    new ParameterizedTypeReference<ResponseObject<WalletAccount>>() {
                    });
            //  this.logger.debug("hitLicenseClient response: {}", response);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("", e);
            ;
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ResponseObject responseObject = objectMapper
                    .readValue(e.getResponseBodyAsString(), ResponseObject.class);
                responseObject
                    .setStatus(ResponseCode.FAILURE.type);//TODO: send 200 error to avoid try catch
                return responseObject;
            } catch (JsonProcessingException j) {
                logger.error("", j);
                String status = ResponseCode.FAILURE.type;
                String description = j.getLocalizedMessage();
                description = description
                    .substring(0, description.length() < 100 ? description.length() : 100);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);

                return new ResponseObject<>(status, description, errors, null);
            }


        } catch (Exception e) {
            logger.error("", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getLocalizedMessage();
            description = description
                .substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }
}
