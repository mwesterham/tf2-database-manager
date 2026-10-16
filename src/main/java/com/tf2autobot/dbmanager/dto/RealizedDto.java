package com.tf2autobot.dbmanager.dto;

import java.util.List;

/**
 * Realized profit detail for one trade. Only accepted trades after the 2026-10-12 direction fix are
 * paired ({@code tracked}); earlier rows may have given/received swapped.
 *
 * @param sold        units this trade sold that were paired with a purchase
 * @param unpairedSold SKUs of units sold here with no recorded purchase (bought before tracking)
 * @param bought      units this trade bought, and the trade that later sold each (null while held)
 */
public record RealizedDto(boolean tracked, List<Sold> sold, List<String> unpairedSold, List<Bought> bought) {

    public record Sold(String sku, double revenueRefined, double costRefined, long buyTradeEventId) {}

    public record Bought(String sku, double costRefined, Long sellTradeEventId, Double revenueRefined) {}

    public static RealizedDto untracked() {
        return new RealizedDto(false, List.of(), List.of(), List.of());
    }
}
