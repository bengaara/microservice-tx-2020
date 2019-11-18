package net.tospay.transaction.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/16/2019, Mon
 **/
public class Error
{
    @JsonProperty("code")
    private String code;

    @JsonProperty("description")
    private String description;

    /**
     * No args constructor for use in serialization
     */
    public Error()
    {
    }

    /**
     * @param description
     * @param code
     */
    public Error(String code, String description)
    {
        super();
        this.code = code;
        this.description = description;
    }

    @JsonProperty("code")
    public String getCode()
    {
        return code;
    }

    @JsonProperty("code")
    public void setCode(String code)
    {
        this.code = code;
    }

    public Error withCode(String code)
    {
        this.code = code;
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

    public Error withDescription(String description)
    {
        this.description = description;
        return this;
    }
}
