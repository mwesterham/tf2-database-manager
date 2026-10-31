package com.tf2autobot.dbmanager.dto;

import java.time.Instant;

/** One completed flip: a unit bought in one accepted trade and sold in a later one (FIFO pairing). */
public record RealizedPairDto(String sku, double costRefined, double revenueRefined, Instant boughtAt, Instant soldAt,
                              long buyTradeEventId, long sellTradeEventId) {}
