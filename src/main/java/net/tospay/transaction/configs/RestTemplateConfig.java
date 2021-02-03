package net.tospay.transaction.configs;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.Amount;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseErrorObject;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.util.Constants;
import net.tospay.transaction.util.Constants.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.ExtractingResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestTemplateConfig extends WebMvcConfigurationSupport {
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Autowired
    private ObjectMapper objectMapper;


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Do any additional configuration here

        builder.additionalMessageConverters(byteArrayHttpMessageConverter())
            .additionalMessageConverters(mappingJackson2HttpMessageConverter())
            .setConnectTimeout(Duration.of(5, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(5, ChronoUnit.SECONDS))
          //  .requestFactory(new HttpComponentsClientHttpRequestFactory())
            .errorHandler(new ExtractingResponseErrorHandler() {

                @Override
                public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
                    super.handleError(response, statusCode);
                    logger.error("RestTemplateConfig restTemplate.handleError {}", response);
                }
            });

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.findAndRegisterModules();


        return builder.build();
    }

//
//    @Bean
//    public RestTemplate getRestTemplate() {
//
//        // HttpHeaders headers = new org.springframework.http.HttpHeaders();
//        //  headers.setContentType(MediaType.APPLICATION_JSON);
//        RestTemplate restTemplate = new RestTemplate();
//        // restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
//
//        restTemplate.getMessageConverters().add(byteArrayHttpMessageConverter());
//        restTemplate.getMessageConverters().add(mappingJackson2HttpMessageConverter());
//
//        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(5000);
//        ((SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout(5000);
//
//   //     restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
//
//        restTemplate.setErrorHandler(new ExtractingResponseErrorHandler() {
//
//            @Override
//            public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
//                super.handleError(response, statusCode);
//                logger.error("RestTemplateConfig restTemplate.handleError {}", response);
//            }
//        });
//
//        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
//
//        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
//        objectMapper.findAndRegisterModules();
//
//        return restTemplate;
//    }

    @Bean
     MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
       //  converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN));
         converter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON,MediaType.TEXT_PLAIN));
        converter.setObjectMapper(objectMapper);

        return converter;
    }

    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {

        converters.add(mappingJackson2HttpMessageConverter());
        converters.add(byteArrayHttpMessageConverter());
        super.configureMessageConverters(converters);
    }
    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        ByteArrayHttpMessageConverter converter = new ByteArrayHttpMessageConverter();
        converter.setSupportedMediaTypes(    Arrays.asList( MediaType.APPLICATION_OCTET_STREAM,  MediaType.APPLICATION_PDF));

        return converter;
    }

    //    @Bean
//    public Jackson2ObjectMapperBuilder objectMapperBuilder(){
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
//       // builder.failOnUnknownProperties(true);
//       // builder.deserializers(BigDecimal.class,BigDecimalMoneyDeserializer.class);
//        return builder;
//    }
    @Bean
    SimpleModule emptyStringAsNullModule() {
        SimpleModule module = new SimpleModule();

        module.addDeserializer(
                Amount.class,
                new StdDeserializer<Amount>(Amount.class) {
                    @Override
                    public Amount deserialize(JsonParser parser, DeserializationContext context)
                            throws IOException {
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

  @Override
  public void addInterceptors(InterceptorRegistry registry)
  {
    registry.addInterceptor(new HandlerInterceptor() {
      @Override
      public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
      {
        logger.debug("MINIMAL: INTERCEPTOR PREHANDLE CALLED");
        String token = request.getParameter("token");
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        if (Constants.LICENSE_ACTIVE) {
          //fill data here add pass to next level
          logger.debug("license active .. continue");

          return true;
        } else {
          // if you opt to not to proceed the request further you can simply return false here
          logger.info("license inactive .. terminate");
          String status = ResponseCode.FAILURE.type;
          String description = ResponseCode.FAILURE.name();
          throw new ResponseErrorObject(new ResponseObject(status, description,
              Arrays.asList(new Error(ResponseCode.LICENSE_EXPIRED.type, ResponseCode.LICENSE_EXPIRED.name())),
              null));

          //return false;
        }

      }

      @Override
      public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception
      {
        logger.debug("MINIMAL: INTERCEPTOR POSTHANDLE CALLED");
        long startTime = (Long)request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        //modified the exisitng modelAndView
       // modelAndView.addObject("executeTime",executeTime);
        logger.debug("[" + handler + "] executeTime : " + executeTime + "ms");

      }

      @Override
      public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) throws Exception
      {
        logger.debug("MINIMAL: INTERCEPTOR AFTERCOMPLETION CALLED");
      }
    })
        .excludePathPatterns(URL.API_VER + Constants.URL.LICENSE + "/**");
  }
}

