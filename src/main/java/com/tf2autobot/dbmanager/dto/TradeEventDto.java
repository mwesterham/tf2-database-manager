package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventDto(
        String offerId,
        String partnerSteamId64,
        double profitInRefined,
        List<TradeItemDto> itemsGiven,
        List<TradeItemDto> itemsReceived,
        String status,
        List<SnapshotDto> pricingSnapshot,
        List<BptfListingDto> bptfListings
) {
    public record TradeItemDto(String sku, double usedPriceRefined) {}
    public record SnapshotDto(String sku, double buyPriceRefined, double sellPriceRefined, String intent,
                              boolean enabled, boolean autoprice, int minStock, int maxStock, boolean autoManaged) {}
    public record BptfListingDto(String sku, String intent,
                                 String steamId, String userName,
                                 Double rawValue, Double metal, Long keys,
                                 String marketName, String details, Long listedAt,
                                 Boolean userPremium, Boolean userOnline, Boolean userBanned,
                                 String userTradeOfferUrl) {}
}
