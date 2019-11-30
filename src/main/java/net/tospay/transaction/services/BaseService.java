package net.tospay.transaction.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;


public class BaseService
{
    Logger logger = LoggerFactory.getLogger(this.getClass());
    //Logger logger = LogManager.getLogger(this.getClass());
    ObjectMapper objectMapper = new ObjectMapper();
}
