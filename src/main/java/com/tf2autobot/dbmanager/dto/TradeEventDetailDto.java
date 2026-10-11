package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventDetailDto(
        long id,
        String offerId,
        String partnerSteamId64,
        double profitInRefined,
        String tradedAt,
        List<TradeItemDto> itemsGiven,
        List<TradeItemDto> itemsReceived,
        String status,
        List<SnapshotEntryDto> pricingSnapshot
) {
    public record TradeItemDto(String sku, double usedPriceRefined) {}
    public record SnapshotEntryDto(String sku, double buyPriceRefined, double sellPriceRefined, String intent,
                                   boolean enabled, boolean autoprice, int minStock, int maxStock, boolean autoManaged) {}
}
