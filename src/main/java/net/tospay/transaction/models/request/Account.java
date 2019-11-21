package net.tospay.transaction.models.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import net.tospay.transaction.configs.Model;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Account extends Model
{
    public Account(){
        super();
    }

    public Account(Model m){
        this.id = m.getId();
    }
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