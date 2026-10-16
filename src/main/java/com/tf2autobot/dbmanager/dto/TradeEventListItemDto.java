package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventListItemDto(
        long id,
        String offerId,
        String partnerSteamId64,
        double profitInRefined,
        String tradedAt,
        List<TradeItemDto> itemsGiven,
        List<TradeItemDto> itemsReceived,
        String status,
        String reason
) {
    public record TradeItemDto(String sku, double usedPriceRefined) {}
}
