package net.tospay.transaction.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Transaction;
import net.tospay.transaction.enums.ResponseCode;
import net.tospay.transaction.models.request.TransactionFetchRequest;
import net.tospay.transaction.models.response.ResponseObject;
import net.tospay.transaction.models.response.TransactionFetchResponse;
import net.tospay.transaction.models.response.TransactionsFetchResponse;
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

        List<TransactionFetchResponse> list = crudServiced
                .fetchSourceAndDestination(request.getUserId(), request.getOffset(),
                        request.getLimit());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, list);
    }

    @PostMapping(Constants.URL.TRANSACTIONS_ID)
    public ResponseObject<List<TransactionFetchResponse>> fetchTransaction(@RequestBody TransactionFetchRequest request)
    {
        logger.info(" {}", request);
        //only retrieve your data

        Optional<Transaction> optional =
                crudServiced.fetchTransactionByTransactionIdAndUserId(request.getTransactionId(), request.getUserId());
        Transaction tr = optional.orElse(new Transaction());

        TransactionsFetchResponse t = new TransactionsFetchResponse();
        t.setAmount(tr.getPayload().getOrderInfo().getAmount().getAmount());
        t.setCurrency(tr.getPayload().getOrderInfo().getAmount().getCurrency());
        tr.getSources().forEach(s -> {
            TransactionFetchResponse res = TransactionFetchResponse.from(s);
            t.getSource().add(res);
        });
        tr.getDestinations().forEach(s -> {
            TransactionFetchResponse res = TransactionFetchResponse.from(s);
            t.getSource().add(res);
        });

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, t);
    }
}
