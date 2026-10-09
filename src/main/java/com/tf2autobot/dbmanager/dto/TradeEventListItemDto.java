package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventListItemDto(
        long id,
        String offerId,
        String partnerSteamId64,
        double profitInRefined,
        String tradedAt,
        List<String> itemsGiven,
        List<String> itemsReceived
) {}
