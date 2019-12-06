package net.tospay.transaction.configs;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class BigDecimalMoneyDeserializer extends StdDeserializer<BigDecimal>
{
    private static final long serialVersionUID = 1L;

    public BigDecimalMoneyDeserializer()
    {
        this(null);
    }

    public BigDecimalMoneyDeserializer(Class<BigDecimal> t)
    {
        super(t);
    }

    @Override
    public BigDecimal deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException
    {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode v = node.get(jp.getCurrentName());
        return v == null ? null : new BigDecimal(v.toString()).setScale(2, RoundingMode.HALF_UP);
    }
}