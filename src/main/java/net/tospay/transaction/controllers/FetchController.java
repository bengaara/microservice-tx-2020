package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<Source> list1 = crudServiced.fetchSources(request.getUserId(), request.getUserType());
        List<Destination> list2 = crudServiced.fetchDestinations(request.getUserId(), request.getUserType());

        List<TransactionFetchResponse> list = new ArrayList<>();

        list1=list1
                .stream()
                .filter(s-> {
                            boolean b = 0 == list2.stream()
                                    .filter(d ->
                                            d.getTransaction().getId().equals(s.getTransaction().getId()) && s.getUserId()
                                                    .equals(d.getUserId()
                                                    )
                                    ).limit(1).count();
                            return b;
                        }
                  ).collect(Collectors.toList());


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
            res.setSourceChannel(s.getType().name());
            res.setType(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());
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
            res.setSourceChannel(s.getType().name());
            res.setType(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());

            list.add(res);
        });
        list.sort(new Comparator<TransactionFetchResponse>() {
            @Override
            public int compare(TransactionFetchResponse o1, TransactionFetchResponse o2) {
                return o2.getDateCreated().compareTo(o1.getDateCreated()) ;
            }
        });




        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, list);
    }
    @PostMapping(Constants.URL.TRANSACTIONS_ALL)
    public ResponseObject<List<TransactionFetchResponse>> fetchTransactions(@Valid @RequestBody TransactionFetchRequest request)
    {
        logger.info(" {}", request);

        List<Source> list1 = crudServiced.fetchSources(request.getUserId(), request.getUserType());
        List<Destination> list2 = crudServiced.fetchDestinations(request.getUserId(), request.getUserType());

        Map<String,TransactionsFetchResponse> transactions = new HashMap<String,TransactionsFetchResponse>();

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
            res.setSourceChannel(s.getType().name());
            res.setType(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());

            TransactionsFetchResponse t =transactions.get(res.gettId());
            if(t==null){
                t= new TransactionsFetchResponse();
                t.setAmount(s.getTransaction().getAmount());
                t.setCurrency(s.getTransaction().getCurrency());
                transactions.put(res.gettId(),t);

            }
            t.getSource().add(res);
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
            res.setSourceChannel(s.getType().name());
            res.setType(s.getTransaction().getTransactionType().name());
            res.setStatus(s.getTransactionStatus().name());

            TransactionsFetchResponse t =transactions.get(res.gettId());
            if(t==null){
                t= new TransactionsFetchResponse();
                t.setAmount(s.getTransaction().getAmount());
                t.setCurrency(s.getTransaction().getCurrency());
                transactions.put(res.gettId(),t);

            }
            t.getDelivery().add(res);
        });
        List<TransactionsFetchResponse> transactionsList = transactions.values().stream()
                .collect(Collectors.toList());




        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, transactionsList);
    }
}
