package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.entity.TradeEventItem;
import com.tf2autobot.dbmanager.entity.TradeRealizedPair;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RealizedProfitServiceTest {

    private long itemIds = 1;

    private TradeEvent trade(long id, Object... dirSkuPrice) {
        TradeEvent t = new TradeEvent();
        t.setId(id);
        t.setStatus("accepted");
        t.setTradedAt(Instant.parse("2026-10-13T00:00:00Z").plusSeconds(id));
        for (int i = 0; i < dirSkuPrice.length; i += 3) {
            TradeEventItem item = new TradeEventItem();
            item.setId(itemIds++);
            item.setTradeEvent(t);
            item.setDirection((String) dirSkuPrice[i]);
            item.setSku((String) dirSkuPrice[i + 1]);
            item.setUsedPriceRefined((Double) dirSkuPrice[i + 2]);
            t.getItems().add(item);
        }
        return t;
    }

    @Test
    void pairsSaleWithEarliestEarlierPurchase() {
        TradeEvent buy1 = trade(1, "received", "31618;6", 12.78, "given", "5002;6", 13.0);
        TradeEvent buy2 = trade(2, "received", "31618;6", 14.00, "given", "5002;6", 14.0);
        TradeEvent sell = trade(3, "given", "31618;6", 19.67, "received", "5002;6", 19.0);

        List<TradeRealizedPair> pairs = RealizedProfitService.match(List.of(buy1, buy2, sell));

        assertEquals(1, pairs.size());
        TradeRealizedPair p = pairs.get(0);
        assertEquals(1, p.getBuyTradeEventId());
        assertEquals(3, p.getSellTradeEventId());
        assertEquals(12.78, p.getCostRefined());
        assertEquals(19.67, p.getRevenueRefined());
    }

    @Test
    void saleWithoutEarlierPurchaseIsUnpaired() {
        TradeEvent sell = trade(1, "given", "31404;6", 13.22, "received", "5002;6", 13.0);
        assertTrue(RealizedProfitService.match(List.of(sell)).isEmpty());
    }

    @Test
    void doesNotPairWithinTheSameTrade() {
        // An item-for-item swap of the same SKU isn't a realized sale
        TradeEvent swap = trade(1, "received", "30;6", 5.0, "given", "30;6", 6.0);
        assertTrue(RealizedProfitService.match(List.of(swap)).isEmpty());
    }

    @Test
    void keysAreCurrencyInItemTradesButPairedInKeyMetalTrades() {
        TradeEvent buyItemWithKey = trade(1, "received", "31622;11", 66.44, "given", "5021;6", 64.44);
        TradeEvent buyKey = trade(2, "received", "5021;6", 64.33, "given", "5002;6", 64.0);
        TradeEvent sellKey = trade(3, "given", "5021;6", 64.44, "received", "5002;6", 64.0);

        List<TradeRealizedPair> pairs = RealizedProfitService.match(List.of(buyItemWithKey, buyKey, sellKey));

        assertEquals(1, pairs.size());
        assertEquals("5021;6", pairs.get(0).getSku());
        assertEquals(2, pairs.get(0).getBuyTradeEventId());
        assertEquals(64.44 - 64.33, pairs.get(0).getRevenueRefined() - pairs.get(0).getCostRefined(), 1e-9);
    }

    @Test
    void metalIsNeverPaired() {
        TradeEvent a = trade(1, "received", "5002;6", 1.0, "given", "5000;6", 0.11);
        TradeEvent b = trade(2, "given", "5002;6", 1.0, "received", "5001;6", 0.33);
        assertTrue(RealizedProfitService.match(List.of(a, b)).isEmpty());
    }
}
