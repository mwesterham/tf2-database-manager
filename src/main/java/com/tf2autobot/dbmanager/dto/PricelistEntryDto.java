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
        Instant createdAt,
        Instant updatedAt
) {
    public static PricelistEntryDto from(PricelistEntry e) {
        CurrenciesDto buy = (e.getBuyKeys() != null || e.getBuyMetal() != null)
                ? new CurrenciesDto(e.getBuyKeys(), e.getBuyMetal()) : null;
        CurrenciesDto sell = (e.getSellKeys() != null || e.getSellMetal() != null)
                ? new CurrenciesDto(e.getSellKeys(), e.getSellMetal()) : null;
        return new PricelistEntryDto(
                e.getSku(), e.isEnabled(), e.isAutoprice(), e.getIntent(),
                e.getMin(), e.getMax(), buy, sell, e.getNote(),
                e.getCreatedAt(), e.getUpdatedAt()
        );
    }
}
