package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.tospay.transaction.models.BaseModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResponseErrorObject extends RuntimeException
{
    public ResponseErrorObject(ResponseObject response){
        this.response=response;
    }
    private ResponseObject response;

    public ResponseObject getResponse() {
        return response;
    }

    public void setResponse(ResponseObject response) {
        this.response = response;
    }
}
