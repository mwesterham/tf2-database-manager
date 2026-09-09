package com.tf2autobot.dbmanager.dto;

import com.tf2autobot.dbmanager.entity.PricelistEntry;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PricelistEntryDto(
        @NotBlank String sku,
        boolean enabled,
        boolean autoprice,
        @NotNull Integer intent,
        int min,
        int max,
        CurrenciesDto buy,
        CurrenciesDto sell,
        String note,
        boolean autoManaged,
        boolean integTestOnly,
        Instant createdAt,
        Instant updatedAt
) {
    public static PricelistEntryDto from(PricelistEntry e) {
        CurrenciesDto buy = (e.getBuyKeys() != null || e.getBuyHalfScrap() != null)
                ? new CurrenciesDto(e.getBuyKeys(), e.getBuyHalfScrap()) : null;
        CurrenciesDto sell = (e.getSellKeys() != null || e.getSellHalfScrap() != null)
                ? new CurrenciesDto(e.getSellKeys(), e.getSellHalfScrap()) : null;
        return new PricelistEntryDto(
                e.getSku(), e.isEnabled(), e.isAutoprice(), e.getIntent(),
                e.getMin(), e.getMax(), buy, sell, e.getNote(), e.isAutoManaged(),
                e.isIntegTestOnly(), e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
