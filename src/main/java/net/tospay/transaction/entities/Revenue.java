package net.tospay.transaction.entities;

import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;


@Projection(types = Revenue.class)
//@JsonIgnoreProperties
public interface Revenue {

    java.util.UUID getUUID();

    String getCurrency();

    LocalDateTime getYear();

    Integer getItems();

    Double getTopup();

    Double getTransfer();

    Double getWithdraw();

    Double getReversal();

    Double getSettlement();

    Double getPayment();

    Double getUtility();

}
