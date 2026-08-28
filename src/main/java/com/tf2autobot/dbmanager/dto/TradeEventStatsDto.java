package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventStatsDto(
        long totalTrades,
        double totalProfitInRefined,
        List<ItemStatDto> mostTradedItems,
        List<ItemProfitDto> mostProfitableItems
) {
    public record ItemStatDto(String sku, long tradeCount) {}
    public record ItemProfitDto(String sku, double totalProfitInRefined) {}
}
