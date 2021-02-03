package net.tospay.transaction.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class BaseController {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    //Logger logger = LogManager.getLogger(this.getClass());
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    RestTemplate restTemplate;

    @Value("{auth.url}")
    String authUrl;



}
