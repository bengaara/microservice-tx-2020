package net.tospay.transaction.models.request;

import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopupValueDestination extends TopupValueSource
{

    @Null
    private Double amount;
}