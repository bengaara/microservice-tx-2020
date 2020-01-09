package net.tospay.transaction.services;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.prowidesoftware.swift.model.SwiftBlock5;
import com.prowidesoftware.swift.model.SwiftTagListBlock;
import com.prowidesoftware.swift.model.Tag;
import com.prowidesoftware.swift.model.field.Field20;
import com.prowidesoftware.swift.model.field.Field25;
import com.prowidesoftware.swift.model.field.Field28C;
import com.prowidesoftware.swift.model.field.Field60F;
import com.prowidesoftware.swift.model.field.Field61;
import com.prowidesoftware.swift.model.field.Field62F;
import com.prowidesoftware.swift.model.field.Field86;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;

import net.tospay.transaction.entities.Report;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.repositories.ReportingRepository;

@Service
public class ReportingService extends BaseService
{
    @Autowired
    RestTemplate restTemplate;

    ReportingRepository reportingRepository;

    CrudService crudService;

    @Value("${kpa_uuid}")
    UUID KPA_UUID;

    @Value("${kpa_account_id}")
    String KPA_ACCOUNT_ID;

    public ReportingService(RestTemplate restTemplate, ReportingRepository reportingRepository, CrudService crudService)
    {
        this.restTemplate = restTemplate;

        this.reportingRepository = reportingRepository;

        this.crudService = crudService;
    }

    public void prepareKPAReports(LocalDateTime to)
    {
        prepareReports(KPA_UUID, to);
    }

    public void prepareReports(UUID userId, LocalDateTime to)
    {
        try {
            logger.info("prepareReports for {} period: {} - {} thread: {}", userId, to, LocalDateTime.now(),
                    Thread.currentThread().getName());

            Optional<Report> optional = reportingRepository.findFirstByUserIdOrderByDateCreatedDesc(userId);
            final LocalDateTime[] lastrun = { to.toLocalDate().atStartOfDay() }; //midnight?
            optional.ifPresent(report -> {
                lastrun[0] = report.getDateTo();
            });

            prepareReports(userId, lastrun[0], to);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    public void prepareReports(UUID userId, LocalDateTime from, LocalDateTime to)
    {
        try {
            logger.info("prepareReports for {} period: {} - {} thread: {}", userId, to, LocalDateTime.now(),
                    Thread.currentThread().getName());

            List<TransactionFetchResponse> list = crudService
                    .fetchSourceAndDestination(userId, from, to);

            createMt940(userId, from, to, list);
        } catch (Exception e) {
            logger.error("{}", e);
        }
    }

    void createMt940(UUID userId, LocalDateTime from, LocalDateTime to, List<TransactionFetchResponse> list)
    {
        final MT940 m = new MT940();

        final BigDecimal[] total = { BigDecimal.ZERO };

        //m.setSender("");
        //m.setReceiver("");

//        SwiftBlock1 b1 = new SwiftBlock1();
//        b1.setApplicationId("");
//        b1.setServiceId("");
//        b1.setLogicalTerminal("");
//        b1.setSessionNumber("");
//        b1.setSequenceNumber("");
//        m.getSwiftMessage().setBlock1(b1);
//
//        SwiftBlock2Input b2 = new SwiftBlock2Input();
//        b2.setMessageType("");
//        b2.setReceiverAddress("");
//        b2.setDeliveryMonitoring("");
//        m.getSwiftMessage().setBlock2(b2);
//
//        SwiftBlock3 block3= new SwiftBlock3();
//        block3.append(new Tag("108", "CTIS"));
//        m.getSwiftMessage().addBlock(block3);

        String date = getDateString(LocalDateTime
                .now());
        m.addField(new Field20("940S"
                + date));//Transaction Reference Number TODO Reference Number 16x for National Bank accounts <940S> followed by the bookdate (YYMMDD), for example “940S121224”. For non-National Bank accounts, the field-20 reference
        m.addField(new Field25(
                KPA_ACCOUNT_ID));//Account Identification 35x. The account number in field-25 will be presented in IBAN format if available. Specifically: <IBAN><space><CURRENCY>

        Optional<Report> optionalOld = reportingRepository.findFirstByUserIdOrderByDateCreatedDesc(userId);
        Report oldReport = optionalOld.orElse(null);
        final Long[] sequenceNumber = { 0l };
        optionalOld.ifPresent(report -> {
            sequenceNumber[0] = report.getReportNumber() + 1;
        });
//        Report oldReport = optional.orElseGet(() ->
//        {
//            Report r = new Report();
//            r.setUserId(userId);
//            return r;
//        });

        Field28C f28c = new Field28C()
                .setSequenceNumber(sequenceNumber[0])
                .setStatementNumber(sequenceNumber[0]);
        m.addField(f28c);
        Field60F f60f = new Field60F() //Opening balance
                .setCurrency("KES")//TODO  hardcoded amount
                .setDate(date)
                .setAmount(total[0])
                .setDCMark("D");
        m.addField(f60f);

        DecimalFormat decimalFormat = new DecimalFormat("#,00");
        list.forEach(transactionFetchResponse -> {

            String DC = transactionFetchResponse.getOperation().substring(0, 1);//Debit/credit D/C
            if ("C".equalsIgnoreCase(DC)) {
                total[0] = total[0].add(new BigDecimal(transactionFetchResponse.getAmount().toString()));
            } else {
                total[0] = total[0].subtract(new BigDecimal(transactionFetchResponse.getAmount().toString()));
            }

            Field61 f61 = new Field61().setValueDate(getDateString(transactionFetchResponse.getDateCreated()))//yymmdd
                    .setEntryDate(getDateString(transactionFetchResponse.getDateCreated()).substring(2))//mmdd
                    .setDCMark(DC)//Debit/credit D/C
                    //.setFundsCode("D")
                    .setAmount(decimalFormat.format(transactionFetchResponse.getAmount()))
                    .setTransactionType("TRF")//TRF = transfer
                    .setIdentificationCode("NONREF")
                    //  .setReferenceForTheAccountOwner("")
                    // .setReferenceOfTheAccountServicingInstitution("")
                    // .setSupplementaryDetails("63992044037");
                    ;
            Field86 f86 = new Field86().setNarrative(transactionFetchResponse.getAccountName())
                    .setNarrativeLine1(transactionFetchResponse.getOperation());
            m.append(new SwiftTagListBlock()
                    .append(f61
                            //Field61.tag("1707240724CY158990,OONMSCNONREF\n178339456010")
                    )
                    .append(
                            f86
                            // Field86.tag("BCIPS HSBCTWTPXXX CNS320670UFRB7SW")
                    )

            );
        });

//        m.append(new SwiftTagListBlock()
//                .append(
//                        Field61.tag("1707240724CY11065844,20NMSCNONREF\n1796000000008")
//                )
//                .append(Field86.tag("BCIPS HSBCHKHHHHKH CNS320670UFRB7SW//31833-GOOD /IMPORT PAYME\n" +
//                        "NT For \n" +
//                        "QINGDAO CHINA"))
//
//        );

        Field62F f62f = new Field62F() //closing balance
                .setAmount(total[0])
                .setCurrency("KES")
                .setDate(date).setDCMark("D");
        m.addField(f62f);

        SwiftBlock5 block5 = new SwiftBlock5();
        block5.append(new Tag("CHK", ""));
        m.getSwiftMessage().addBlock(block5);

        logger.debug("{}", m.message());

        Report report = new Report();
        report.setUserId(userId);
        report.setDateFrom(from);
        report.setDateTo(to);
        //  report.setPayload(m);
        report.setPayloadString(m.message());
        report.setReportNumber(sequenceNumber[0]);

        //reportingRepository.save(report);
        reportingRepository.saveAndFlush(report);
    }

    String getDateString(LocalDateTime date)
    {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        // LocalDate localDate = LocalDate.parse(date, formatter);
        return formatter.format(date.toLocalDate());
    }
}

