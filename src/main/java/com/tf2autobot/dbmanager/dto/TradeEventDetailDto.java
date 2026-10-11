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
        List<SnapshotEntryDto> pricingSnapshot,
        List<BptfListingDto> bptfListings
) {
    public record TradeItemDto(String sku, double usedPriceRefined) {}
    public record SnapshotEntryDto(String sku, double buyPriceRefined, double sellPriceRefined, String intent,
                                   boolean enabled, boolean autoprice, int minStock, int maxStock, boolean autoManaged) {}
    public record BptfListingDto(String sku, String intent, int keys, long halfScrap, int stock, int limit) {}
}
