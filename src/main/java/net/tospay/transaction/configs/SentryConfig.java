package net.tospay.transaction.configs;

import io.sentry.Sentry;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SentryConfig {

//  @Bean
//  public HandlerExceptionResolver sentryExceptionResolver() {
//    return new io.sentry.spring.SentryExceptionResolver();
//  }
//
//  @Bean
//  public ServletContextInitializer sentryServletContextInitializer() {
//    return new io.sentry.spring.SentryServletContextInitializer();
//  }

  @Value("${sentry.DSN}")
  private String sentryDsn;

  @PostConstruct
  private void initializeSentry() {
    if (sentryDsn != null) {
      Sentry.init(sentryDsn);
    }

      //   Sentry.getStoredClient().setServerName("transaction");

      //Added as tags to Sentry event
      //  Sentry.getStoredClient().addTag("testing-dynamicTag1", "1.0");
//
//        Sentry.getStoredClient().addShouldSendEventCallback(new ShouldSendEventCallback() {
//            @Override
//            public boolean shouldSend(Event event) {
//                if (event.getMessage().contains("foo")) {
//                    return false;
//                }
//                return true;
//            }
//        });


  }

}