package net.tospay.transaction.services;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.response.ChargeResponse;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class ChargeRestService
{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private ChargeRestInterface chargeRestInterface;

    @Autowired
    public ChargeRestService(Retrofit retrofit)
    {
        this.chargeRestInterface = retrofit.create(ChargeRestInterface.class);
    }

    public ChargeResponse fetchCharge(ChargeRequest chargeRequest)
    {
        try {

            logger.debug("", chargeRequest);
            Call<ChargeResponse> call = chargeRestInterface.call(chargeRequest);
            Response<ChargeResponse> res = call.execute();
            return res.body();
        } catch (IOException e) {
            logger.error("", e);
            return null;
        }
    }

    private @NotNull ResponseEntity<Map<String, Object>> getMapResponseEntity(Call<Map<Object, Object>> call,
            Map<String, Object> map)
    {
        try {
            Response<Map<Object, Object>> response = call.execute();

            if (response.isSuccessful()) {
                map.put("data", response.body());
                map.put("message", "Success");
            } else {
                if (Objects.nonNull(response.errorBody())) {
                    map.put("error", new ObjectMapper().readValue(response.errorBody().string(), Map.class));
                }
                map.put("message", "Fail");
            }

            return ResponseEntity.status(response.code()).body(map);
        } catch (IOException e) {
            e.printStackTrace();
            map.put("message", "failed with exception");
            map.put("data", e.getMessage());
            return ResponseEntity.status(500).body(map);
        }
    }
}
