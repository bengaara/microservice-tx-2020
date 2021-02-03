package net.tospay.transaction.services;

import java.util.Objects;
import java.util.UUID;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.enums.UserType;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.StatementResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Map;

@Service
public class EmailService extends BaseService {


    @Value("${mail.url}")
    String mailUrl;

    @Value("${report.statement}")
    Resource fullStatementReport;
    @Value("${report.statement}")
    String fullStatementReportString;
    @Value("${report.statement_logo}")
    Resource fullStatementLogo;
    @Value("${report.statement_logo}")
    String fullStatementLogoString;

    @Autowired
    ResourceLoader resourceLoader;

    //    public void send(String title,List<String> recipients, String fileContent,String ext) {
//        File tempFile = null;
//        try {
//            tempFile = File.createTempFile(title, ext!=null?ext:".csv");
//            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
//            bw.write(fileContent);
//            bw.close();
//            send(title,recipients, tempFile);
//        } catch (IOException e) {
//            logger.error("", e);
//        }
//
//    }
    public void send(UUID userId, UserType type,String name, Notify.Category category, BaseModel data, byte[] fileContent, String ext) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(category.name() + "-" + name, ext != null ? ext : ".csv");
            //    BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            //    bw.write(fileContent);
//            FileOutputStream out = new FileOutputStream(tempFile);
//                out.write(fileContent);
            Files.write(tempFile.toPath(), fileContent, StandardOpenOption.TRUNCATE_EXISTING);

            send(userId,type, category, data, tempFile);
        } catch (Exception e) {
            logger.error("", e);
        }

    }

    public void send(UUID userId, UserType type, Notify.Category category, BaseModel data, File tempFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

//            EmailRequest request = new EmailRequest();
//            request.setRecipient(recipients);
//          //  request.setCc(recipients);
//            request.setText("Attached is a copy of the transaction report");
//            request.setSubject(title);
//            request.setType("MT940");
            NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();
            request.setNotificationType(category);
            request.setStatus(TransactionStatus.SUCCESS);
            request.setRecipientId(Objects.toString(userId));
            request.setRecipientType(type);
            request.setData(data);

            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tempFile));
            body.add("json", objectMapper.writeValueAsString(request));


            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<>(body, headers);

            //    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(request, headers);
            logger.debug(" {} {}",mailUrl, requestEntity);
            ResponseEntity<ResponseObject> response = restTemplate
                    .postForEntity(mailUrl, requestEntity, ResponseObject.class);
            logger.debug(" {} ", response);

        } catch (Exception e) {
            logger.error("", e);
        }

    }

    public byte[] generatePdf(StatementResponse statementResponse, String pin) {
        try {

            Map<String, Object> mapObj = objectMapper.convertValue(statementResponse, Map.class);

            logger.debug("{} {} {} {} {} {}", fullStatementReport.exists(), fullStatementReport.getURL(), fullStatementLogo.exists(), fullStatementLogo.getURL());
            if ("jar".equals(fullStatementReport.getURL().getProtocol())) {
                String jarDir = System.getProperty("user.dir");
                String report = "file:" + jarDir + File.separator + fullStatementReportString.replace("classpath:", "");
                String reportLogo = "file:" + jarDir + File.separator + fullStatementLogoString.replace("classpath:", "");
                logger.debug("path relative {} {} {} ", jarDir, report, reportLogo);
                fullStatementReport = resourceLoader.getResource(report);// new ClassPathResource(report);
                fullStatementLogo = resourceLoader.getResource(reportLogo);
            }
            logger.debug("path: {} {} {} ", fullStatementLogo.getFile().getPath(), fullStatementReport.getFile().getPath());
            mapObj.put("logo", fullStatementLogo.getFile().getPath());
            statementResponse.getItems().add(0,null);//hack to get 1st record showing
            JRBeanCollectionDataSource jrBeanCollectionDataSource = new JRBeanCollectionDataSource(statementResponse.getItems());//Arrays.asList()

            mapObj.put("transactionDataSource", jrBeanCollectionDataSource);
            mapObj.put("items", statementResponse.getItems());

            System.setProperty("java.awt.headless", "true");
            JasperReport jasperReport = JasperCompileManager.compileReport(fullStatementReport.getInputStream());
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, mapObj, jrBeanCollectionDataSource);

            if(pin !=null) {
                jasperPrint.setProperty("net.sf.jasperreports.export.pdf.user.password", pin);
            }
            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            logger.error("", e);
        }

        return null;
    }
}
