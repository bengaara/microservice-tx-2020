package net.tospay.transaction.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prowidesoftware.swift.model.SwiftTagListBlock;
import com.prowidesoftware.swift.model.field.*;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import net.tospay.transaction.entities.Report;
import net.tospay.transaction.entities.SourceDestinationMerge;
import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.AccountType;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.models.Account;
import net.tospay.transaction.repositories.ReportingRepository;
import net.tospay.transaction.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ReportingService extends BaseService {


    ReportingRepository reportingRepository;

    TransactionRepository transactionRepository;

    CrudService crudService;

    DashboardService dashboardService;

    EmailService emailService;

//    @Value("${kpa_uuid}")
//    UUID KPA_UUID;
//
//    @Value("${kpa_account_id}")
//    String KPA_ACCOUNT_ID;

    public ReportingService(RestTemplate restTemplate, ReportingRepository reportingRepository, TransactionRepository transactionRepository, CrudService crudService, DashboardService dashboardService
            , EmailService emailService) {
        this.restTemplate = restTemplate;

        this.reportingRepository = reportingRepository;
        this.transactionRepository = transactionRepository;

        this.crudService = crudService;
        this.emailService = emailService;
        this.dashboardService = dashboardService;

    }


    public void prepareAllReports(LocalDate from, LocalDate to) {

        Transaction t = new Transaction();

        List<UUID> list = transactionRepository.findPartnerWithTransactionInRange(null, from.atStartOfDay(), to.atStartOfDay());

        logger.info("prepareReports for accounts: {}", list.size());

        list.forEach(uuid -> {
            Account account = getPartnerAccount(uuid);
            String title = "Transaction report for " + account.getUserId() + " for period"
                    + from + " - " + to;
            logger.info(" {}", title);
            Report report = prepareReports(account, from, to);
            if (report != null) {

                //send email
                emailService.send(account.getUserId(),account.getUserType(),
                    account.getName(),
                    Notify.Category.MT940,null,report.getMT940PayloadString().getBytes(),".csv");
            }
        });


    }

    public Account getPartnerAccount(UUID uuid) {
        Object o = transactionRepository.findPartnerAccount(uuid);

        Account account = null;
        try {
            account = objectMapper.readValue(o.toString(), Account.class);
        } catch (JsonProcessingException e) {
            logger.error("",e);
        }

        return account;


    }

    public Report prepareReports(Account account, LocalDate from, LocalDate to) {
        try {
            logger.info("prepareReports for {}  {} period: {} - {} {} ", account.getUserId(), from, to);
            //optimise.. fetch from history if report already prepared
            Optional<Report> opt = reportingRepository.findFirstByUserIdAndDateFromAndDateTo(account.getUserId(), from.atStartOfDay(), to.atStartOfDay());
            final Report[] report = {null};
            opt.ifPresentOrElse(report1 -> {
                logger.info("found cached report {}", report1.getId());
                report[0] = report1;
            }, () -> {
                List<SourceDestinationMerge> list = transactionRepository.findUserTransactionInRange(account.getUserId(), from.atStartOfDay(), to.atStartOfDay());//  , new OffsetBasedPageRequest(0, 10000)

                //JpaSort.unsafe(SourceDestinationMerge.DATE_CREATED).descending())
                report[0] = createMt940(account, from, to, list);

            });

            return report[0];
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    Report createMt940(Account account, LocalDate from, LocalDate to, List<SourceDestinationMerge> list) {
        final MT940 m = new MT940();

        final BigDecimal[] total = {BigDecimal.ZERO, BigDecimal.ZERO};//0 opening bal,1 closing bal
        AtomicReference<String> currency = new AtomicReference<>();

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
        m.addField(new Field20("TOSPAY"
                + date));//Transaction Reference Number TODO Reference Number 16x for National Bank accounts <940S> followed by the bookdate (YYMMDD), for example “940S121224”. For non-National Bank accounts, the field-20 reference
        m.addField(new Field25(account.getUserId().toString()));//Account Identification 35x. The account number in field-25 will be presented in IBAN format if available. Specifically: <IBAN><space><CURRENCY>

        Optional<Report> optionalOld = reportingRepository.findFirstByUserIdOrderByDateCreatedDesc(account.getUserId());
        Report oldReport = optionalOld.orElse(null);
        final Integer[] sequenceNumber = {1};
        optionalOld.ifPresent(report -> {
            //if 1st report of the day.. start at 1
            if (LocalDateTime.now().getDayOfYear() == report.getDateCreated().getDayOfYear()) {
                sequenceNumber[0] = report.getReportNumber() + 1;
            }
            total[0] = report.getOpeningBalance();
            total[1] = total[0];
        });
        //NB: opening balance is wallet balance - each wallet hit keeps track of last balance
        //get 1st wallet transaction:
        list.stream().filter(sourceDestinationMerge -> {
            return AccountType.WALLET.equals(sourceDestinationMerge.getSource().getPayload().getAccount().getType());
        }).findFirst().ifPresent(sourceDestinationMerge -> {
            logger.debug("found wallet transaction. last balance: {} {}", sourceDestinationMerge.getSource().getId(), sourceDestinationMerge.getSource().getAvailableBalance());
            total[0] = sourceDestinationMerge.getSource().getAvailableBalance();
            total[1] = total[0];
            currency.set(sourceDestinationMerge.getSource().getPayload().getTotal().getCurrency());
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
                .setCurrency(currency.get())
                .setDate(date)
                .setAmount(total[0])
                .setDCMark("C");
        m.addField(f60f);

        DecimalFormat decimalFormat = new DecimalFormat("#,##");
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);

        list.forEach(sourceDestinationMerge -> {
            String DC = "D";//Debit/credit D/C
            if ("C".equalsIgnoreCase(DC)) {
                total[1] = total[1].add(sourceDestinationMerge.getSource().getPayload().getTotal().getAmount());
            } else {
                total[1] = total[1].subtract(sourceDestinationMerge.getSource().getPayload().getTotal().getAmount());
            }

            Field61 f61 = new Field61().setValueDate(getDateString(sourceDestinationMerge.getSource().getDateCreated()))//yymmdd
                    .setEntryDate(getDateString(sourceDestinationMerge.getSource().getDateCreated()).substring(2))//mmdd
                    .setDCMark(DC)//Debit/credit D/C
                    //.setFundsCode("D")
                    .setAmount(decimalFormat.format(sourceDestinationMerge.getSource().getPayload().getTotal().getAmount()))
                    .setTransactionType("TRF")//TRF = transfer
                    .setIdentificationCode("NONREF")
                    //.setReferenceOfTheAccountServicingInstitution()
                    //  .setReferenceForTheAccountOwner("")
                    // .setReferenceOfTheAccountServicingInstitution("")
                    // .setSupplementaryDetails("63992044037");
                    ;
            Field86 f86 = new Field86().setNarrative(sourceDestinationMerge.getSource().getPayload().getAccount().getName())
                    .setNarrativeLine1("C");
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
                .setAmount(total[1])
                .setCurrency("KES")
                .setDate(date).setDCMark("D");
        m.addField(f62f);

//        SwiftBlock5 block5 = new SwiftBlock5();
//        block5.append(new Tag("CHK", ""));
//        m.getSwiftMessage().addBlock(block5);

        logger.debug("{}", m.message());

        Report report = new Report();
        report.setUserId(account.getUserId());
        report.setEmail(account.getEmail());
        report.setDateFrom(from.atStartOfDay());
        report.setDateTo(to.atStartOfDay());
        //  report.setPayload(m);
        report.setMT940PayloadString(m.message());
        report.setReportNumber(sequenceNumber[0]);
        report.setOpeningBalance(total[0]);
        report.setClosingBalance(total[1]);
        report.setTransactionCount(list.size());

        reportingRepository.save(report);
        //reportingRepository.saveAndFlush(report);
        return report;
    }

//    Report createNBK(Report report, List<TransactionFetchResponse> list)
//    {
//        final BigDecimal[] total = { report.getOpeningBalance(), report.getOpeningBalance() };//0 opening bal
//
//        StringBuilder NBKPayloadString = new StringBuilder();
//
//        list.forEach(transactionFetchResponse -> {
//
//            NBKPayloadString.append(KPA_ACCOUNT_ID + " ");//account number
//            NBKPayloadString.append(getNBKDateString(transactionFetchResponse.getDateCreated()) + " ");
//            NBKPayloadString.append(getNBKDateString(transactionFetchResponse.getDateCreated()) + " ");
//            String DC = transactionFetchResponse.getOperation().substring(0, 1);//Debit/credit D/C
//            if ("C".equalsIgnoreCase(DC)) {
//                total[1] = total[1].add(new BigDecimal(transactionFetchResponse.getAmount().toString()));
//                NBKPayloadString.append("0.00 " + transactionFetchResponse.getAmount() + " ");
//                NBKPayloadString.append(total[1] + " ");
//                NBKPayloadString.append(transactionFetchResponse.getAccountName() + " ");
//            } else {
//                total[1] = total[1].subtract(new BigDecimal(transactionFetchResponse.getAmount().toString()));
//                NBKPayloadString.append(transactionFetchResponse.getAmount() + " 0.00 ");
//                NBKPayloadString.append(total[1] + " ");
//                NBKPayloadString.append(" OUTGOING " + transactionFetchResponse.getAccountName() + " ");
//            }
//            NBKPayloadString.append(" 0 ");//what?
//        });
//
//        logger.debug("{}", NBKPayloadString.toString());
//        report.setNBKPayloadString(NBKPayloadString.toString());
//
//        //reportingRepository.save(report);
//        reportingRepository.saveAndFlush(report);
//        return report;
//    }

    public String getDateString(LocalDateTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        // LocalDate localDate = LocalDate.parse(date, formatter);
        return formatter.format(date.toLocalDate());
    }

    public String getNBKDateString(LocalDateTime date) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // LocalDate localDate = LocalDate.parse(date, formatter);
        return formatter.format(date);
    }
}

