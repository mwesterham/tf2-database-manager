package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.entity.TradeEventItem;
import com.tf2autobot.dbmanager.repository.TradeEventItemRepository;
import com.tf2autobot.dbmanager.repository.TradeEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TradeEventService {

    private final TradeEventRepository eventRepo;
    private final TradeEventItemRepository itemRepo;

    public TradeEventService(TradeEventRepository eventRepo, TradeEventItemRepository itemRepo) {
        this.eventRepo = eventRepo;
        this.itemRepo = itemRepo;
    }

    public void record(String offerId, String partnerSteamId64, double profitInRefined,
                       List<String> itemsGiven, List<String> itemsReceived) {
        TradeEvent event = new TradeEvent();
        event.setOfferId(offerId);
        event.setPartnerSteamId64(partnerSteamId64);
        event.setProfitInRefined(profitInRefined);

        List<TradeEventItem> items = new ArrayList<>();
        for (String sku : itemsGiven) {
            TradeEventItem item = new TradeEventItem();
            item.setSku(sku);
            item.setDirection("given");
            item.setTradeEvent(event);
            items.add(item);
        }
        for (String sku : itemsReceived) {
            TradeEventItem item = new TradeEventItem();
            item.setSku(sku);
            item.setDirection("received");
            item.setTradeEvent(event);
            items.add(item);
        }
        event.setItems(items);
        eventRepo.save(event);
    }

    @Transactional(readOnly = true)
    public TradeEventStatsDto getStats() {
        long totalTrades = eventRepo.count();
        double totalProfit = eventRepo.sumProfit().orElse(0.0);

        List<TradeEventStatsDto.ItemStatDto> mostTraded = itemRepo.findMostTradedItems()
                .stream()
                .map(r -> new TradeEventStatsDto.ItemStatDto(
                        (String) r[0],
                        ((Number) r[1]).longValue()))
                .toList();

        List<TradeEventStatsDto.ItemProfitDto> mostProfitable = itemRepo.findMostProfitableItems()
                .stream()
                .map(r -> new TradeEventStatsDto.ItemProfitDto(
                        (String) r[0],
                        ((Number) r[1]).doubleValue()))
                .toList();

        return new TradeEventStatsDto(totalTrades, totalProfit, mostTraded, mostProfitable);
    }
}
