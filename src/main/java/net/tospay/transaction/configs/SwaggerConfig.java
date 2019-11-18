package net.tospay.transaction.configs;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author : Clifford Owino
 * @Email : owinoclifford@gmail.com
 * @since : 9/5/2019, Thu
 **/
@Configuration
@EnableSwagger2
public class SwaggerConfig
{
    @Bean
    public Docket api()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/api/**"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo()
    {
        return new ApiInfo(
                "Coop Middleware",
                "A bridge between Tospay and Coop Bank",
                "0.0.1",
                "https://tospay.net",
                new Contact(
                        "Clifford Owino",
                        "https://tospay.net",
                        "clifford@tospay.net"
                ),
                "info@tospay.net",
                "License of API",
                Collections.emptyList());
    }
}
