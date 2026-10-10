package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.DailyProfitDto;
import com.tf2autobot.dbmanager.dto.TradeEventListItemDto;
import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.entity.TradeEventItem;
import com.tf2autobot.dbmanager.repository.TradeEventItemRepository;
import com.tf2autobot.dbmanager.repository.TradeEventRepository;
import com.tf2autobot.dbmanager.repository.TradeEventSpecs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TradeEventService {

    private final TradeEventRepository eventRepo;
    private final TradeEventItemRepository itemRepo;
    private final SteamUserService steamUserService;
    private final SchemaService schemaService;

    public TradeEventService(TradeEventRepository eventRepo, TradeEventItemRepository itemRepo,
                             SteamUserService steamUserService, SchemaService schemaService) {
        this.eventRepo = eventRepo;
        this.itemRepo = itemRepo;
        this.steamUserService = steamUserService;
        this.schemaService = schemaService;
    }

    public void record(String offerId, String partnerSteamId64, double profitInRefined,
                       List<String> itemsGiven, List<String> itemsReceived, String status) {
        if (eventRepo.existsByOfferId(offerId)) return;
        TradeEvent event = new TradeEvent();
        event.setOfferId(offerId);
        event.setPartnerSteamId64(partnerSteamId64);
        event.setProfitInRefined(profitInRefined);
        event.setStatus(status != null ? status : "accepted");

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
    public List<DailyProfitDto> getDailyProfit() {
        return eventRepo.findDailyProfit().stream()
                .map(r -> new DailyProfitDto(
                        r[0].toString(),
                        ((Number) r[1]).doubleValue(),
                        ((Number) r[2]).longValue()))
                .toList();
    }

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("id", "tradedAt", "profitInRefined", "partnerSteamId64");

    @Transactional(readOnly = true)
    public Page<TradeEventListItemDto> list(int page, int size, String sort, String direction,
                                            String partner, String offerId,
                                            Double minProfit, Double maxProfit,
                                            String dateFrom, String dateTo,
                                            String item, String status) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "id";
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(dir, safeSort));
        java.util.Set<String> nameMatches = (partner != null && !partner.isBlank())
                ? steamUserService.findIdsByName(partner)
                : java.util.Set.of();
        java.util.Set<Integer> itemDefindexes = (item != null && !item.isBlank())
                ? schemaService.findDefindexesByName(item)
                : java.util.Set.of();
        Specification<TradeEvent> spec = TradeEventSpecs.withFilters(partner, nameMatches, offerId, minProfit, maxProfit, dateFrom, dateTo, item, itemDefindexes, status);
        return eventRepo.findAll(spec, pageable).map(e -> {
            List<String> given = e.getItems().stream()
                    .filter(i -> "given".equals(i.getDirection()))
                    .map(TradeEventItem::getSku)
                    .toList();
            List<String> received = e.getItems().stream()
                    .filter(i -> "received".equals(i.getDirection()))
                    .map(TradeEventItem::getSku)
                    .toList();
            return new TradeEventListItemDto(
                    e.getId(),
                    e.getOfferId(),
                    e.getPartnerSteamId64(),
                    e.getProfitInRefined(),
                    e.getTradedAt().toString(),
                    given,
                    received,
                    e.getStatus()
            );
        });
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
