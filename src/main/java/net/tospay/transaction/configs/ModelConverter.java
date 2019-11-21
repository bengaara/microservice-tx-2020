package net.tospay.transaction.configs;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.persistence.AttributeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class  ModelConverter<T extends Model> implements AttributeConverter<T, String>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectMapper objectMapper = new ObjectMapper();



    public ModelConverter ()
    {

    }

    @Override
    public String convertToDatabaseColumn(T customerInfo)
    {
        String customerInfoJson = null;
        try {
            customerInfoJson = objectMapper.writeValueAsString(customerInfo);
        } catch (final JsonProcessingException e) {
            logger.error("JSON writing error {}", e);
        }

        return customerInfoJson;
    }

    @Override
    public T convertToEntityAttribute(String customerInfoJSON)
    {

        T customerInfo = null;
        try {

            customerInfo = (T) objectMapper.readValue(customerInfoJSON, Model.class);
        } catch (final IOException e) {
            logger.error("JSON reading error {}", e);
        }

        return customerInfo;
    }
}