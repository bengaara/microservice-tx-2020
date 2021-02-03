package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prowidesoftware.swift.model.SwiftTagListBlock;
import com.prowidesoftware.swift.model.field.*;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import net.tospay.transaction.entities.License;
import net.tospay.transaction.entities.Report;
import net.tospay.transaction.entities.SourceDestinationMerge;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.StoreResponse;
import net.tospay.transaction.models.request.LicenseObject;
import net.tospay.transaction.models.request.TransferOutgoingRequest;
import net.tospay.transaction.models.response.Error;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.repositories.LicenseRepository;
import net.tospay.transaction.repositories.ReportingRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class LicenseService extends BaseService {

    ReportingRepository reportingRepository;

    LicenseRepository licenseRepository;

    CrudService crudService;

    @Value("${license.publish.url}")
    String licensePublishUrl;


    public LicenseService(RestTemplate restTemplate, LicenseRepository licenseRepository, CrudService crudService
         ) {

        this.restTemplate = restTemplate;

        this.licenseRepository = licenseRepository;

        this.crudService = crudService;

    }
    public void publishDetails()
    {
        logger.debug("publish license details {}");

        LicenseObject licenseObject = generateLicenseKey();

        License license = new License();
        license.setDeviceInfo(licenseObject);
//        license.setDaysLicensed();
//        license.setDaysRunning();
//        license.setDegradeDate();
//        license.setDegradeItems();
//        license.setLicense();
//        license.setEndDate();
//        license.setStartDate();
//        license.setPublishResponse();
//        license.setStatusResponse();

        licenseRepository.save(license);

    //    ResponseObject response = hitLicenseClient(licenseObject);

    }
    public LicenseObject generateLicenseKey()
    {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();
        CentralProcessor centralProcessor = hardwareAbstractionLayer.getProcessor();
        ComputerSystem computerSystem = hardwareAbstractionLayer.getComputerSystem();


        String vendor = operatingSystem.toString();//getManufacturer();
        String processorSerialNumber = computerSystem.toString();//getSerialNumber();
        String processorIdentifier = centralProcessor.getProcessorIdentifier().toString();//.getProcessorID();
        int processors = centralProcessor.getLogicalProcessorCount();

        LicenseObject licenseObject = new LicenseObject();
        licenseObject.setVendor(vendor);
        licenseObject.setProcessorIdentifier(processorIdentifier);
        licenseObject.setProcessors(processors);
        licenseObject.setProcessorSerialNumber(processorSerialNumber);
        licenseObject.setSystemBootTime(operatingSystem.getSystemBootTime());
        licenseObject.setSystemUpTime(operatingSystem.getSystemUptime());
        licenseObject.setServiceId(UUID.randomUUID().toString());
        licenseObject.setLaunchDate(Calendar.getInstance().getTime());
        hardwareAbstractionLayer.getNetworkIFs().forEach(networkIF -> {
            licenseObject.getMacAddress().add(networkIF.getMacaddr());
            licenseObject.getIpv4Address().add(networkIF.getIPv4addr().length ==0?null:networkIF.getIPv4addr()[0]);
            licenseObject.getIpv6Address().add(networkIF.getIPv6addr().length ==0?null:networkIF.getIPv6addr()[0]);
        });

        this.logger.debug("licenseObject: {} {}", licenseObject);
        return licenseObject;
    }

    public ResponseObject hitLicenseClient(LicenseObject request ) {
        try {
            String url = licensePublishUrl;
            this.logger.debug("hitLicenseClient request: {} {}", url, request);//.setHideData(false)

            HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity entity = new HttpEntity<LicenseObject>(request, headers);

            ResponseEntity<ResponseObject<StoreResponse>> response = restTemplate.exchange(url, HttpMethod.POST, entity, new ParameterizedTypeReference<ResponseObject<StoreResponse>>() {
            });
          //  this.logger.debug("hitLicenseClient response: {}", response);

            return response.getBody();
        } catch (HttpStatusCodeException e) {
            logger.error("", e);
          ObjectMapper objectMapper = new ObjectMapper();
            try {
                ResponseObject responseObject = objectMapper.readValue(e.getResponseBodyAsString(), ResponseObject.class);
                responseObject.setStatus(ResponseCode.FAILURE.type);//TODO: send 200 error to avoid try catch
                return responseObject;
            } catch (JsonProcessingException j) {
                logger.error("", j);
                String status = ResponseCode.FAILURE.type;
                String description = j.getLocalizedMessage();
                description = description.substring(0, description.length() < 100 ? description.length() : 100);
                ArrayList<Error> errors = new ArrayList<>();
                Error error = new Error(status, description);
                errors.add(error);

                return new ResponseObject<>(status, description, errors, null);
            }


        } catch (Exception e) {
            logger.error("", e);

            String status = ResponseCode.FAILURE.type;
            String description = e.getLocalizedMessage();
            description = description.substring(0, description.length() < 100 ? description.length() : 100);
            ArrayList<Error> errors = new ArrayList<>();
            Error error = new Error(status, description);
            errors.add(error);

            return new ResponseObject<>(status, description, errors, null);
        }
    }
}


