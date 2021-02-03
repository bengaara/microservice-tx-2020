package net.tospay.transaction.controllers;

import net.tospay.transaction.entities.Report;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.models.request.ReportFetchRequest;
import net.tospay.transaction.repositories.TransactionRepository;
import net.tospay.transaction.services.EmailService;
import net.tospay.transaction.services.ReportingService;
import net.tospay.transaction.util.Constants;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API_VER)
public class ReportController extends BaseController {

    EmailService emailService;
    ReportingService reportingService;
    TransactionRepository transactionRepository;


    public ReportController(EmailService emailService, ReportingService reportingService, TransactionRepository transactionRepository) {
        this.emailService = emailService;
        this.reportingService = reportingService;
        this.transactionRepository = transactionRepository;
    }

    @PostMapping(value = Constants.URL.REPORT,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] fetch(@Valid @RequestBody ReportFetchRequest request) {
        // logger.info(" {}", request);
//request.getUserInfo().getTypeId(),

        Account account = reportingService.getPartnerAccount(request.getUserInfo().getUserId());

        Report report = reportingService.prepareReports(account,
                request.getFrom(), request.getTo());

        if (report == null) {
            logger.info("no report found for {} {} {} ", request);
            return null;
        }
            emailService.send(request.getUserInfo().getUserId(),request.getUserInfo().getTypeId(),
                request.getUserInfo().getName(),Notify.Category.MT940,null, report.getMT940PayloadString().getBytes(),".csv");

//        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
//            logger.info("send to emails {} ", request.getEmail());
//
//
//        }

        byte[] byteArr = report.getMT940PayloadString().getBytes();
        return byteArr;
//        File tempFile = File.createTempFile("report-" + LocalDate.now(), ".tmp");
//        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
//        writer.append(report.getMT940PayloadString());
//        writer.close();


        // byte[] array = Files.readAllBytes(Paths.get("/path/to/file"));

//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//        headers.add("Pragma", "no-cache");
//        headers.add("Expires", "0");
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentLength(tempFile.length())
//                .contentType(MediaType.parseMediaType("application/octet-stream"))
//                .body(tempFile);
    }


    @PostMapping(value = Constants.URL.REPORT_ALL)
    public String fetchAll(@RequestBody ReportFetchRequest request) {

        reportingService.prepareAllReports(request.getFrom(), request.getTo());

        return "preparing reports.. check email";

    }
}
