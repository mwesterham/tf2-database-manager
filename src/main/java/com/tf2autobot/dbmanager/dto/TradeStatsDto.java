package com.tf2autobot.dbmanager.dto;

import com.tf2autobot.dbmanager.entity.TradeStats;

import java.math.BigDecimal;
import java.time.Instant;

public record TradeStatsDto(
        Instant since,
        int tradesAccepted,
        int tradesDeclined,
        BigDecimal totalProfitInRefined
) {
    public static TradeStatsDto from(TradeStats s) {
        return new TradeStatsDto(
                s.getSince(), s.getTradesAccepted(), s.getTradesDeclined(), s.getTotalProfitInRefined()
        );
    }
}
