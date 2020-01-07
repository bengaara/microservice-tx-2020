package net.tospay.transaction.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

        List<Source> list1 = crudServiced
                .fetchSources(request.getUserId(), request.getUserType(), request.getOffset(), request.getLimit());
        List<Destination> list2 = crudServiced
                .fetchDestinations(request.getUserId(), request.getUserType(), request.getOffset(), request.getLimit());

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
