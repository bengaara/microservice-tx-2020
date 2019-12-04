package net.tospay.transaction.services;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.UserInfo;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;

@Service
public class AuthService extends BaseService
{
    RestTemplate restTemplate;



    @Value("${auth.account.info.url}")
    String authAccountInfoUrl;

    public AuthService(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
    }



    public ResponseObject<UserInfo>  getUserInfo(Account account)
    {
        try {
            ResponseObject<UserInfo>  response = hitAuthAccount(account);
            return response;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    public ResponseObject<UserInfo> hitAuthAccount(Account request)
    {
        try {

            logger.debug(" {}", request);
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<Account>(request, headers);

            logger.debug(" {}", request);
            ResponseObject<UserInfo> response =
                    restTemplate.postForObject(authAccountInfoUrl, entity, ResponseObject.class);
            logger.debug(" {}", response);

            return response;
        } catch (HttpClientErrorException e) {
            logger.error(" {}", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getResponseBodyAsString();
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);
            logger.error(" {}", description);
            return new ResponseObject<>(status, description, errors, null);
        } catch (Exception e) {
            logger.error("{}", e);
            return null;
        }
    }
}
