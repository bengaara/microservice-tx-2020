package net.tospay.transaction.services;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.tospay.transaction.enums.Notify;
import net.tospay.transaction.enums.TransactionStatus;
import net.tospay.transaction.models.BaseModel;
import net.tospay.transaction.models.request.NotifyTransferOutgoingRequest;
import net.tospay.transaction.models.response.StatementResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

@Service
public class SMSService extends BaseService {


    NotifyService notifyService;

    SMSService(NotifyService notifyService) {
        this.notifyService = notifyService;
    }

    public void sendNotify(StatementResponse statementResponse) {
        logger.info("send statement to customer {} {}", statementResponse);
        NotifyTransferOutgoingRequest request = new NotifyTransferOutgoingRequest();

        request.setNotificationType(Notify.Category.MINI_STATEMENT);
        request.setStatus(TransactionStatus.SUCCESS);
        request.setRecipientId(Objects.toString(statementResponse.getCustomer().getUserId()));
        request.setRecipientType(statementResponse.getCustomer().getTypeId());
        BaseModel baseModel = new BaseModel();

        String[] statement ={""};// {"date,TXID,status,desc,amount \n"};
        final String[] sta = {""};
        statementResponse.getItems().stream().forEach(item -> {

            sta[0] +=item.transactionId ;
            sta[0] += " " + item.action;
            sta[0] += " " + item.currency;
            sta[0] +=" " + (item.outgoing != null ? item.outgoing.toString() :
                (item.incoming==null?null:item.incoming.toString()));
            sta[0] += " " + item.dateCreatedFormattedShort + "\n";

            if (statement[0].length() + sta[0].length() <= 160) {
                statement[0] +=  sta[0];
                sta[0]="";
            } else {
                logger.debug(" statement sms length {} truncated", statement[0].length());
                baseModel.setAdditionalProperty("statement", statement[0]);
                request.setData(baseModel);
                HashMap response = notifyService.hitNotify(request);
                statement[0] = "";
            }

        });
        ///send last bit
        if(statement[0].length()>0){
            baseModel.setAdditionalProperty("statement", statement[0]);
            request.setData(baseModel);
            HashMap response = notifyService.hitNotify(request);
            statement[0] = "";
        }


    }


}
