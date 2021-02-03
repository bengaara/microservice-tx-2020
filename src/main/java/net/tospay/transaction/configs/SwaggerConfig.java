package net.tospay.transaction.configs;

import java.util.Collections;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
//@EnableSwagger2
public class SwaggerConfig {

//  @Autowired
//  private ServletContext servletContext;
//
//  @Bean
//  public Docket api() {
//
//    return new Docket(DocumentationType.SWAGGER_2)
//       // .host("localhost")
//       // .directModelSubstitute(LocalDate.class, Date.class)
////        .pathProvider(new RelativePathProvider(servletContext) {
////          @Override
////          public String getApplicationBasePath() {
////            return "/docs";
////          }
////        })
//        .select()
//        .apis(RequestHandlerSelectors.any())
//        .paths(PathSelectors.any())
//        .build();
//  }
//  @Override
//  public void addResourceHandlers(ResourceHandlerRegistry registry) {
//    String baseUrl = StringUtils.trimTrailingCharacter(this.baseUrl, '/');
//    registry.
//        addResourceHandler(baseUrl + "/swagger-ui/**")
//        .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//        .resourceChain(false);
//  }
//
//  @Override
//  public void addViewControllers(ViewControllerRegistry registry) {
//    registry.addViewController(baseUrl + "/swagger-ui/")
//        .setViewName("forward:" + baseUrl + "/swagger-ui/index.html");
//  }
//
//  @Override
//  public void addCorsMappings(CorsRegistry registry) {
//    registry
//        .addMapping("/api/**")
//        .allowedOrigins("http://editor.swagger.io");
//    registry
//        .addMapping("/v2/api-docs.*")
//        .allowedOrigins("http://editor.swagger.io");
//  }
//
//  @Bean
//  public Docket api() {
//    return new Docket(DocumentationType.SWAGGER_2)
//        .select()
//        .apis(RequestHandlerSelectors.any())
//        .paths(PathSelectors.any())//PathSelectors.ant("/api/**"))
//        .build()
//        .apiInfo(apiInfo());
//  }
//
//  private ApiInfo apiInfo() {
//    return new ApiInfo(
//        "Transaction Service",
//        "A Tospay transaction Switch to all value Stores",
//        "0.0.1",
//        "https://tospay.net",
//        null,
//        "info@tospay.net",
//        "#",
//        Collections.emptyList());
//  }

//  @Bean
//  public InternalResourceViewResolver defaultViewResolver() {
//    return new InternalResourceViewResolver();
//  }

}
