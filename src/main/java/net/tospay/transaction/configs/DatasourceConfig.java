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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

//@ContextConfiguration
//@ConfigurationProperties(prefix = "spring.datasource")
@Component
public class DatasourceConfig
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Value("${jdbc.sslrootcert}")
    Resource sslRootCert;

    @Value("${jdbc.sslcert}")
    Resource sslCert;

    @Value("${jdbc.sslkey}")
    Resource sslKey;

    @Value("${jdbc.datasource.url}")
    String datasourceUrl;

    @Value("${jdbc.ssl.mode}")
    String sslMode;

    @Value("${jdbc.driverClass}")
    String driverClassName;

    @Value("${jdbc.username}")
    String username;

    @Value("${jdbc.ssl.state}")
    Boolean sslState;

    @Bean
    public DataSource dataSource()
    {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        //dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(dataSourceUrl());
        dataSource.setUsername(username);
//        dataSource.setPassword();
        return dataSource;
    }

    private String dataSourceUrl()
    {

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("ssl", String.valueOf(sslState));
        requestMap.put("sslmode", sslMode);
        try {

            //if files from jar, we cant use the absolute path. we need to create temp files:
            if ("jar".equals(sslRootCert.getURL().getProtocol())) {
                requestMap.put("sslrootcert", copyFile(sslRootCert, File.createTempFile("sslrootcert", null).toPath()));
                requestMap.put("sslkey", copyFile(sslKey, File.createTempFile("sslKey", null).toPath()));
                requestMap.put("sslcert", copyFile(sslCert, File.createTempFile("sslCert", null).toPath()));
            } else {
                requestMap.put("sslrootcert", sslRootCert.getFile().getPath());
                requestMap.put("sslkey", sslKey.getFile().getPath());
                requestMap.put("sslcert", sslCert.getFile().getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datasourceUrl + "?" + requestMap.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

//
//    private Properties hibernateProperties() {
//        Properties properties = new Properties();
//        properties.put("hibernate.dialect", driverClassName);
//        properties.put("hibernate.classloading.use_current_tccl_as_parent", false);
//        return properties;
//    }

    String copyFile(Resource source, Path dest)
    {
        try (InputStream s = source.getInputStream()) {  //FileOutputStream os = new FileOutputStream(dest.toString());

            Files.copy(s, dest, StandardCopyOption.REPLACE_EXISTING);
            //os.close();

            return dest.toString();
        } catch (Exception e) {
            LOG.error("exception", e);
            return null;
        }
    }
}