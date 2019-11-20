package net.tospay.transaction.models.request;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.SourceType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopupValueDestination extends TopupValueSource
{

    @Null
    private Double amount;
}