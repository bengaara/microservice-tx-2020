package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.AccountType.AccountSubType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionLimit;
import net.tospay.transaction.repositories.LicenseRepository;
import net.tospay.transaction.repositories.ReportingRepository;
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
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionLimitService extends BaseService {
    @Autowired
    RestTemplate restTemplate;


    @Value("${transaction_limits.url}")
    String transactionLimitUrl;


    public TransactionLimitService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }


    public  ResponseObject<TransactionLimit> hitTransactionLimitService(String id, UserType userType,
        AccountSubType accountSubType) {
        try {
            String url = transactionLimitUrl.replace("{id}",id).replace("{type}",userType.name());
            if(accountSubType !=null){
                url=   url.replace("{sub_type}",accountSubType.name());
            }
            this.logger.debug("hitTransactionLimitService request: {} {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ForexObject>(null, headers);

            ResponseEntity<ResponseObject<TransactionLimit>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<ResponseObject<TransactionLimit>>() {
            });
          //  this.logger.debug("hitLicenseClient response: {}", response);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("", e);
            ;
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ResponseObject responseObject = objectMapper.readValue(e.getResponseBodyAsString(), ResponseObject.class);
                responseObject.setStatus(ResponseCode.FAILURE.type);//TODO: send 200 error to avoid try catch
                return responseObject;
            } catch (JsonProcessingException j) {
                logger.error("", j);
                String status = ResponseCode.FAILURE.type;
                String description = j.getLocalizedMessage();
                description = description.substring(0, description.length() < 100 ? description.length() : 100);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);

                return new ResponseObject<>(status, description, errors, null);
            }


        } catch (Exception e) {
            logger.error("", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getLocalizedMessage();
            description = description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }
}


