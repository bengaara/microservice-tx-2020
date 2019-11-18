package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;

import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.UserInfoResponse;

import static net.tospay.transaction.models.response.ResponseCode.INVALID_REQUEST;

public class BaseController
{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestTemplate restTemplate;

    public <T extends ResponseObject> ResponseObject<T> mapResponse(T response)
    {

        try {
            if (!ResponseCode.SUCCESS.code.equalsIgnoreCase(response.getStatus())) {//on error map?
                String status =
                        response.getStatus() != null ? response.getStatus() : INVALID_REQUEST.responseCode;
                String description = response.getDescription() != null ? response.getDescription() :
                        ResponseCode.GENERAL_ERROR.name();
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);
                switch (ResponseCode.valueOfCode(response.getStatus())) {

                    //case GENERAL_ERROR:
                    default:
                        status =
                                response.getStatus() != null ? response.getStatus() : INVALID_REQUEST.responseCode;
                        description = response.getDescription() != null ? response.getDescription() :
                                ResponseCode.GENERAL_ERROR.name();
                        errors = new ArrayList<>();
                        error = new Error(status, description);
                        errors.add(error);

                        break;
                }

                return new ResponseObject(status, description, errors, response);
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return new ResponseObject(ResponseCode.SUCCESS.code, ResponseCode.SUCCESS.code, null, response);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> exception(Exception e)
    {
        logger.error("", e);

        Error error = new Error();
        error.setCode("000");
        error.setDescription(e.getMessage());

        return new ResponseEntity<>(Arrays.asList(error), HttpStatus.BAD_REQUEST);
    }

    public UserInfoResponse verifyUserToken(String token)
    {
        try {
            Map<String, String> stringStringMap = new HashMap<>();
            stringStringMap.put("token", token);

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<String>(stringStringMap.toString(), headers);

            UserInfoResponse userInfoResponse =
                    restTemplate.postForObject(authUrl, entity, UserInfoResponse.class);
            logger.debug("", userInfoResponse);

            return userInfoResponse;
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }
}