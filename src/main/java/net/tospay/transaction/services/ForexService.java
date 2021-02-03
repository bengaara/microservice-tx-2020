package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
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
public class ForexService extends BaseService {
    @Autowired
    RestTemplate restTemplate;

    ReportingRepository reportingRepository;

    LicenseRepository licenseRepository;

    CrudService crudService;

    @Value("${forex.url}")
    String forexUrl;


    public ForexService(RestTemplate restTemplate, LicenseRepository licenseRepository, CrudService crudService
         ) {
        this.restTemplate = restTemplate;

    }

    public ResponseObject hitForex(Amount origin,Amount destination ) {

        ForexObject forexObject = new ForexObject(origin,destination);
        ResponseObject res =  hitForex(forexObject);

        return res;

    }

     ResponseObject hitForex(ForexObject request ) {
        try {
            String url = forexUrl;
            this.logger.debug("hitForex request: {} {}", url, request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ForexObject>(request, headers);

            ResponseEntity<ResponseObject<ForexObject>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<ResponseObject<ForexObject>>() {
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


