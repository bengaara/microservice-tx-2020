package net.tospay.transaction.services;

import net.tospay.transaction.models.request.ChargeRequest;
import net.tospay.transaction.models.response.ChargeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
public interface ChargeRestInterface
{
    /**
     * @param request
     * @return
     */
    @POST("/charge/1.0.0/")
    Call<ChargeResponse> call(@Body ChargeRequest request);
}
