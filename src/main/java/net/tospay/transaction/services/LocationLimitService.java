package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import net.tospay.transaction.enums.AccountType.AccountSubType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.request.ForexObject;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.LocationFetch;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionLimit;
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
public class LocationLimitService extends BaseService {
    @Autowired
    RestTemplate restTemplate;


    @Value("${location_limits.url}")
    String locationLimitUrl;


    public LocationLimitService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;

    }


    public  ResponseObject<LocationFetch> hitLocationLimitService(UUID id) {
        try {
            String url = locationLimitUrl.replace("{id}", Objects.toString(id));

            this.logger.debug("hitTransactionLimitService request: {} {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<ForexObject>(null, headers);

            ResponseEntity<ResponseObject<LocationFetch>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<ResponseObject<LocationFetch>>() {
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
    private static final double r2d = 180.0D / 3.141592653589793D;
    private static final double d2r = 3.141592653589793D / 180.0D;
    private static final double d2km = 111189.57696D * r2d;
    public  double meters(double lt1, double ln1, double lt2, double ln2) {
        final double x = lt1 * d2r;
        final double y = lt2 * d2r;
        return Math.acos( Math.sin(x) * Math.sin(y) + Math.cos(x) * Math.cos(y) * Math.cos(d2r * (ln1 - ln2))) * d2km;
    }
}


