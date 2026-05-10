package com.workshop.outbox.before.api;

import java.math.BigDecimal;
import java.util.List;

public record PlaceOrderRequest(
        String customerId,
        List<LineItem> items) {
    public record LineItem(String skuId, int quantity, BigDecimal unitPrice) {}
}
