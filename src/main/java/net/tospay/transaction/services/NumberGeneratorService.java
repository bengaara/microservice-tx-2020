package net.tospay.transaction.services;

import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.TransactionType;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Country;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.request.TransactionIdRequest;
import net.tospay.transaction.models.response.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

@Service
public class NumberGeneratorService extends BaseService {


    @Autowired
    NotifyService notifyService;

    @Autowired
    CrudService crudService;

    @Value("${numbergenerator.transaction.url}")
    String numberGeneratorTransactionUrl;

    boolean transactionIdGeneratorUp = false;
    boolean generatingSkippedTx = false;

    public NumberGeneratorService(RestTemplate restTemplate, NotifyService notifyService) {
        this.restTemplate = restTemplate;
        this.notifyService = notifyService;


    }

    public static String generateRandomBase64Token(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token); //base64 encoding
    }


    public void generateTransactionId(Transaction transaction) {
        try {
            this.logger.debug("generateTransactionId {} ");
            UserInfo userInfo = transaction.getPayload().getUserInfo();
            Country country = transaction.getPayload().getSource().get(0).getAccount().getCountry();
            country = country != null ? country : userInfo.getCountry();

            String trId = this.hitTransactionIdGenerator(userInfo.getTypeId(), transaction.getType(), country.getIso());
            if (trId != null) {
                transaction.setTransactionId(trId);

               if(transaction.getReversalChild()!=null) {
                   transaction.getReversalChild().setTransactionId(transaction.getTransactionId()+ "-R");
               }

                crudService.saveTransaction(transaction);
                //service back on - check skipped transactions
                if (!transactionIdGeneratorUp) {
                    transactionIdGeneratorUp = true;
                    this.generateSkippedTransactionId();
                }

            } else {
                this.logger.debug("failed generateTransactionId for {}  \n job will re attempt later", transaction.getId());
                transactionIdGeneratorUp = false;

            }
        } catch (Exception e) {
            this.logger.error("", e);

        }
    }


    String hitTransactionIdGenerator(UserType userType, TransactionType transactionType, String countryCode) {
        try {
            this.logger.debug("generateTransactionId {} {} {} ", userType, transactionType, countryCode);
            TransactionIdRequest request = new TransactionIdRequest();
            request.setUserType(userType);
            request.setCountry(countryCode);
            request.setTransactionType(transactionType);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<TransactionIdRequest>(request, headers);
            this.logger.debug("generateTransactionId {}", request);
            ResponseEntity<ResponseObject> response = this.restTemplate.postForEntity(this.numberGeneratorTransactionUrl, entity, ResponseObject.class);
            this.logger.debug(" {}", response);
            ResponseObject<String> obj = response.getBody();
            return obj.getData();
        } catch (Exception e) {
            this.logger.error("", e);
            return null;
        }
    }

    public void generateSkippedTransactionId() {
        try {
            this.logger.debug("generateSkippedTransactionId ");
            if (generatingSkippedTx) {
                this.logger.debug("generatingSkippedTx .. skip generateSkippedTransactionId ");
                return;
            }
            generatingSkippedTx = true;
            List<Transaction> list = crudService.findByProcessedNullTransactionId();

            for (Transaction transaction : list) {
                generateTransactionId(transaction);
                if (transaction.getTransactionId() == null) {
                    this.logger.debug("generateTransactionId is still down. Exit ");
                    generatingSkippedTx = false;
                    return;
                } else {
                    notifyService.notifySource(transaction);
                    notifyService.notifyDestination(transaction);
                }

            }

        } catch (Exception e) {
            this.logger.error("", e);
            generatingSkippedTx = false;

        }
    }

}
