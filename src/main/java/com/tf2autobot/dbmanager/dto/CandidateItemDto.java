package com.tf2autobot.dbmanager.dto;

public record CandidateItemDto(
        String sku,
        String marketName,
        int activeBuyCount,
        int activeSellCount,
        int buyActivity,
        int sellActivity,
        double bestBuyMetal,
        double bestSellMetal,
        double spreadPct,
        double estRoi2wk
) {}
