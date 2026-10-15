package com.tf2autobot.dbmanager.dto;

import java.time.Instant;

/** One unit the bot received in an accepted trade, valued at the price that trade used (its buy price then). */
public record PurchaseDto(String sku, long tradeEventId, String offerId, Instant tradedAt, double usedPriceRefined) {}
