package com.tf2autobot.dbmanager.dto;

import java.util.List;

public record TradeEventDto(
        String offerId,
        String partnerSteamId64,
        double profitInRefined,
        List<String> itemsGiven,
        List<String> itemsReceived
) {}
