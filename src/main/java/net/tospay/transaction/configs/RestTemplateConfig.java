package net.tospay.transaction.configs;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.tospay.transaction.models.Amount;

@Configuration
public class RestTemplateConfig
{
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate getRestTemplate()
    {

        // HttpHeaders headers = new org.springframework.http.HttpHeaders();
        //  headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        return restTemplate;
    }

    //    @Bean
//    public Jackson2ObjectMapperBuilder objectMapperBuilder(){
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//       // builder.failOnUnknownProperties(true);
//       // builder.deserializers(BigDecimal.class,BigDecimalMoneyDeserializer.class);
//        return builder;
//    }
    @Bean
    SimpleModule emptyStringAsNullModule()
    {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(
                Amount.class,
                new StdDeserializer<Amount>(Amount.class)
                {
                    @Override
                    public Amount deserialize(JsonParser parser, DeserializationContext context)
                            throws IOException
                    {
                        JsonNode n = parser.getCodec().readTree(parser);
                        JsonNode node = n.get("amount");
                        if (node == null) {
                            logger.debug("null amount {}", n);
                            return null;
                        }
                        if (!node.iterator().hasNext()) {
                            node = n;
                        }

                        //  while (node.fieldNames().hasNext()) {
                        String currency =
                                node.get("currency") == null ? null : node.get("currency").toString().replace("\"", "");
                        BigDecimal b = node.get("amount") == null ? null :
                                new BigDecimal(
                                        node.get("amount").toString().replace("\"", "").equalsIgnoreCase("") ? "0" :
                                                node.get("amount").toString().replace("\"", ""))
                                        .setScale(2, RoundingMode.HALF_UP);
                        ///  parser.clearCurrentToken();
                        //    String type = node.get("type") == null ? null : node.get("type").textValue();
                        return new Amount(b, currency);
                        //      }

                    }
                });

        return module;
    }
}

