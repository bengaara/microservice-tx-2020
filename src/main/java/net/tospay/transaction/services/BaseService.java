package net.tospay.transaction.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;


public class BaseService {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    //Logger logger = LogManager.getLogger(this.getClass());
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

}
