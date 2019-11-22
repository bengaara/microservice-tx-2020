package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.request.TransactionFetchRequest;
import net.tospay.transaction.models.response.BaseResponse;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.services.CrudService;
import net.tospay.transaction.util.Constants;

@org.springframework.web.bind.annotation.RestController
@RequestMapping(Constants.URL.API + "/v1")
public class FetchController extends BaseController
{
    @Autowired CrudService crudServiced;

    ObjectMapper mapper = new ObjectMapper();

    public FetchController(CrudService crudServiced)
    {
        this.crudServiced = crudServiced;
    }

    @PostMapping(Constants.URL.TRANSACTIONS_FETCH)
    public ResponseObject<List<TransactionFetchResponse>> fetch(@Valid @RequestBody TransactionFetchRequest request)
    {
        logger.info(" {}", request);

        List<Source> list1 = crudServiced.fetchSources(request.getUserId(), request.getUserType());
        List<Destination> list2 = crudServiced.fetchDestinations(request.getUserId(), request.getUserType());

        List<TransactionFetchResponse> list = new ArrayList<>();

        list1.forEach(s -> {
            TransactionFetchResponse res = new TransactionFetchResponse();
            res.setAmount(s.getAmount());
            res.setCharge(s.getCharge().toString());
            res.setCurrency(s.getCurrency());
            res.setDateCreated(s.getDateCreated());
            res.setDateUpdated(s.getDateModified());
            res.setTransactionId(s.getTransaction().getTransactionId());
            res.setTransactionTransferId(s.getId().toString());
            res.settId(s.getTransaction().getId().toString());
            res.setSourceChannel(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());
            res.setType(s.getType().name());
            list.add(res);
        });
        list2.forEach(s -> {
            TransactionFetchResponse res = new TransactionFetchResponse();
            res.setAmount(s.getAmount());
            res.setCharge(s.getCharge().toString());
            res.setCurrency(s.getCurrency());
            res.setDateCreated(s.getDateCreated());
            res.setDateUpdated(s.getDateModified());
            res.setTransactionId(s.getTransaction().getTransactionId());
            res.setTransactionTransferId(s.getId().toString());
            res.settId(s.getTransaction().getId().toString());
            res.setSourceChannel(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());
            res.setType(s.getType().name());
            list.add(res);
        });

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, list);
    }
}
