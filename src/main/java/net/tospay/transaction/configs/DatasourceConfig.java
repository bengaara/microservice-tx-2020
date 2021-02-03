package net.tospay.transaction.configs;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")
@Configuration
public class DatasourceConfig {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Value("${jdbc.sslrootcert}")
    Resource sslRootCert;
    @Value("${jdbc.sslrootcert}")
    String sslRootCertString;

    @Value("${jdbc.sslcert}")
    Resource sslCert;
    @Value("${jdbc.sslcert}")
    String sslCertString;

    @Value("${jdbc.sslkey}")
    Resource sslKey;
    @Value("${jdbc.sslkey}")
    String sslKeyString;

    @Value("${spring.datasource.url}")
    String datasourceUrl;

    @Value("${spring.datasource.username}")
    String username;

    @Value("${jdbc.ssl.mode}")
    String sslMode;

    @Value("${jdbc.driverClass}")
    String driverClassName;


    @Value("${jdbc.ssl.state}")
    Boolean sslState;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //dataSource.setDriverClassName(driverClassName);
        String url = dataSourceUrl();
        LOG.debug("dataSourceUrl {}", url);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
//        dataSource.setPassword();
      //  dataSource.setDriverClassName(driverClassName);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    private String dataSourceUrl() {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("stringtype", "unspecified");
        requestMap.put("ssl", String.valueOf(sslState));
        requestMap.put("sslmode", sslMode);
        try {

            LOG.debug("{} {} {} {} {} {}", sslRootCert.isReadable(), sslRootCert.getURL(), sslKey.isReadable(), sslKey.getURL(), sslCert.isReadable(), sslCert.getURL());
            //if files from jar, we cant use the absolute path. we need to create temp files:
            if ("jar".equals(sslRootCert.getURL().getProtocol())) {
                String jarDir  = System.getProperty("user.dir");

                LOG.debug("jarDir {}", jarDir);
                requestMap.put("sslrootcert", jarDir + File.separator + sslRootCertString.replace("classpath:",""));
                requestMap.put("sslkey",jarDir + File.separator + sslKeyString.replace("classpath:",""));
                requestMap.put("sslcert", jarDir + File.separator + sslCertString.replace("classpath:",""));

//                requestMap.put("sslrootcert", copyFile(sslRootCert, File.createTempFile("sslrootcert", "crt").toPath()));
//                requestMap.put("sslkey", copyFile(sslKey, File.createTempFile("sslKey", "key").toPath()));
//                requestMap.put("sslcert", copyFile(sslCert, File.createTempFile("sslCert", "crt").toPath()));
            } else {

                requestMap.put("sslrootcert", sslRootCert.getFile().getPath());
                requestMap.put("sslkey", sslKey.getFile().getPath());
                requestMap.put("sslcert", sslCert.getFile().getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datasourceUrl + "?" + requestMap.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&")) ;
    }

//
//    private Properties hibernateProperties() {
//        Properties properties = new Properties();
//        properties.put("hibernate.dialect", driverClassName);
//        properties.put("hibernate.classloading.use_current_tccl_as_parent", false);
//        return properties;
//    }

    String copyFile(Resource source, Path dest) {
        try (InputStream s = source.getInputStream()) {  //FileOutputStream os = new FileOutputStream(dest.toString());

            Files.copy(s, dest, StandardCopyOption.REPLACE_EXISTING);
            //os.close();

            return dest.toString();
        } catch (Exception e) {
            LOG.error("", e);
            return null;
        }
    }
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(false);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA : ");
        return loggingFilter;
    }
}
