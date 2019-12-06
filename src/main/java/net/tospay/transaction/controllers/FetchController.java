package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tospay.transaction.entities.Destination;
import net.tospay.transaction.entities.Source;
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

        List<Source> list1 = crudServiced.fetchSources(request.getUserId(), request.getUserType(), request.getOffset(), request.getLimit());
        List<Destination> list2 = crudServiced.fetchDestinations(request.getUserId(), request.getUserType(), request.getOffset(), request.getLimit());

        List<TransactionFetchResponse> list = new ArrayList<>();

        list1 = list1
                .stream()
                .filter(s -> {
                            boolean b = 0 == list2.stream()
                                    .filter(d ->
                                            d.getTransaction().getId().equals(s.getTransaction().getId()) && s.getPayload()
                                                    .getAccount().getUserId()
                                                    .equals(d.getPayload().getAccount().getUserId()
                                                    )
                                    ).limit(1).count();
                            return b;
                        }
                ).collect(Collectors.toList());

        list.addAll(list1.stream().map(TransactionFetchResponse::from).collect(Collectors.toList()));
        list.addAll(list2.stream().map(TransactionFetchResponse::from).collect(Collectors.toList()));

        list.sort(new Comparator<TransactionFetchResponse>()
        {
            @Override
            public int compare(TransactionFetchResponse o1, TransactionFetchResponse o2)
            {
                return o2.getDateCreated().compareTo(o1.getDateCreated());
            }
        });

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, list);
    }

    @GetMapping(Constants.URL.TRANSACTIONS_ID)
    public ResponseObject<List<TransactionFetchResponse>> fetchTransaction(@PathVariable String transactionId)
    {
        logger.info(" {}", transactionId);

        Optional<Transaction> optional = crudServiced.fetchTransactionByTransactionId(transactionId);
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

    @PostMapping(Constants.URL.TRANSACTIONS_ALL)
    public ResponseObject<List<TransactionFetchResponse>> fetchTransactions(
            @Valid @RequestBody TransactionFetchRequest request)
    {
        logger.info(" {}", request);

        List<Source> list1 = crudServiced.fetchSources(request.getUserId(), request.getUserType(), request.getOffset(),request.getLimit());
        List<Destination> list2 =
                crudServiced.fetchDestinations(request.getUserId(), request.getUserType(), request.getOffset(),request.getLimit());

        Map<String, TransactionsFetchResponse> transactions = new HashMap<String, TransactionsFetchResponse>();

        list1.forEach(s -> {
            TransactionFetchResponse res = TransactionFetchResponse.from(s);
            TransactionsFetchResponse t = transactions.get(res.gettId());
            if (t == null) {
                t = new TransactionsFetchResponse();
                t.setAmount(s.getPayload().getTotal().getAmount());
                t.setCurrency(s.getPayload().getTotal().getCurrency());
                transactions.put(res.gettId(), t);
            }
            t.getSource().add(res);
        });
        list2.forEach(s -> {
            TransactionFetchResponse res = TransactionFetchResponse.from(s);
            TransactionsFetchResponse t = transactions.get(res.gettId());
            if (t == null) {
                t = new TransactionsFetchResponse();
                t.setAmount(s.getPayload().getTotal().getAmount());
                t.setCurrency(s.getPayload().getTotal().getCurrency());
                transactions.put(res.gettId(), t);
            }
            t.getDelivery().add(res);
        });
        List<TransactionsFetchResponse> transactionsList = transactions.values().stream()
                .collect(Collectors.toList());

        return new ResponseObject(ResponseCode.SUCCESS.type, ResponseCode.SUCCESS.name(), null, transactionsList);
    }
}
