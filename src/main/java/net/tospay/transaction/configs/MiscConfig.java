package net.tospay.transaction.configs;

import io.sentry.spring.SentryExceptionResolver;
import java.util.Arrays;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")

@Configuration
public class MiscConfig {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

//
//    @Bean
//    public Tracer jaegerTracer() {
//      //   Tracer tracer = io.jaegertracing.Configuration.fromEnv("TransactionService").getTracer();
//
//        SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv().withType("const").withParam(1);
//        ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv();//.withLogSpans(true);
//       return io.jaegertracing.Configuration.fromEnv("TransactionService").withSampler(samplerConfig).withReporter(reporterConfig)
//            .getTracer();
//
//
////        Tracer tracer = new JaegerTracer.Builder("TransactionService")
////                .withReporter(new InMemoryReporter())
////                .withSampler(new ConstSampler(true))
////                .withMetrics(new Metrics(metricsFactory))
////                .build();
////
////        Tracer tracer =new Configuration("spring-boot", new io.jaegertracing.Configuration.SamplerConfiguration(),
////                new io.jaegertracing.Configuration.ReporterConfiguration())
////                .getTracer();
//
//
//    }

}
