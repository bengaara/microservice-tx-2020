package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TopupCardAccountRequest
{
    @JsonProperty("id")
    private String id;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}