package net.tospay.transaction.models.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/16/2019, Mon
 **/
public class ResponseObject<T>
{
    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("error")
    private List<Error> error = null;

    @JsonProperty("data")
    private T data;

    /**
     * No args constructor for use in serialization
     */
    public ResponseObject()
    {
    }

    /**
     * @param error
     * @param status
     * @param description
     * @param data
     */
    public ResponseObject(String status, String description, List<Error> error, T data)
    {
        super();
        this.status = status;
        this.description = description;
        this.error = error;
        this.data = data;
    }

    @JsonProperty("status")
    public String getStatus()
    {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status)
    {
        this.status = status;
    }

    public ResponseObject withStatus(String status)
    {
        this.status = status;
        return this;
    }

    @JsonProperty("description")
    public String getDescription()
    {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description)
    {
        this.description = description;
    }

    public ResponseObject withDescription(String description)
    {
        this.description = description;
        return this;
    }

    @JsonProperty("error")
    public List<Error> getError()
    {
        return error;
    }

    @JsonProperty("error")
    public void setError(ArrayList<Error> error)
    {
        this.error = error;
    }

    public ResponseObject withError(ArrayList<Error> error)
    {
        this.error = error;
        return this;
    }

    @JsonProperty("data")
    public T getData()
    {
        return data;
    }

    @JsonProperty("data")
    public void setData(T data)
    {
        this.data = data;
    }

    public ResponseObject withData(T data)
    {
        this.data = data;
        return this;
    }
}
