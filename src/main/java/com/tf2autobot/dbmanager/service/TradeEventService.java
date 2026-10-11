package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.DailyProfitDto;
import com.tf2autobot.dbmanager.dto.TradeEventDetailDto;
import com.tf2autobot.dbmanager.dto.TradeEventListItemDto;
import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.entity.TradeEventBptfListing;
import com.tf2autobot.dbmanager.entity.TradeEventItem;
import com.tf2autobot.dbmanager.entity.TradeEventSnapshot;
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
                       List<com.tf2autobot.dbmanager.dto.TradeEventDto.TradeItemDto> itemsGiven,
                       List<com.tf2autobot.dbmanager.dto.TradeEventDto.TradeItemDto> itemsReceived,
                       String status,
                       List<com.tf2autobot.dbmanager.dto.TradeEventDto.SnapshotDto> pricingSnapshot,
                       List<com.tf2autobot.dbmanager.dto.TradeEventDto.BptfListingDto> bptfListings) {
        if (eventRepo.existsByOfferId(offerId)) return;
        TradeEvent event = new TradeEvent();
        event.setOfferId(offerId);
        event.setPartnerSteamId64(partnerSteamId64);
        event.setProfitInRefined(profitInRefined);
        event.setStatus(status != null ? status : "accepted");

        List<TradeEventItem> items = new ArrayList<>();
        if (itemsGiven != null) {
            for (var dto : itemsGiven) {
                TradeEventItem item = new TradeEventItem();
                item.setSku(dto.sku());
                item.setDirection("given");
                item.setUsedPriceRefined(dto.usedPriceRefined());
                item.setTradeEvent(event);
                items.add(item);
            }
        }
        if (itemsReceived != null) {
            for (var dto : itemsReceived) {
                TradeEventItem item = new TradeEventItem();
                item.setSku(dto.sku());
                item.setDirection("received");
                item.setUsedPriceRefined(dto.usedPriceRefined());
                item.setTradeEvent(event);
                items.add(item);
            }
        }
        event.setItems(items);

        List<TradeEventSnapshot> snapshots = new ArrayList<>();
        if (pricingSnapshot != null) {
            for (var dto : pricingSnapshot) {
                TradeEventSnapshot snap = new TradeEventSnapshot();
                snap.setSku(dto.sku());
                snap.setBuyPriceRefined(dto.buyPriceRefined());
                snap.setSellPriceRefined(dto.sellPriceRefined());
                snap.setIntent(dto.intent());
                snap.setEnabled(dto.enabled());
                snap.setAutoprice(dto.autoprice());
                snap.setMinStock(dto.minStock());
                snap.setMaxStock(dto.maxStock());
                snap.setAutoManaged(dto.autoManaged());
                snap.setTradeEvent(event);
                snapshots.add(snap);
            }
        }
        event.setPricingSnapshot(snapshots);

        List<TradeEventBptfListing> bptfSnap = new ArrayList<>();
        if (bptfListings != null) {
            for (var dto : bptfListings) {
                TradeEventBptfListing l = new TradeEventBptfListing();
                l.setSku(dto.sku());
                l.setIntent(dto.intent());
                l.setSteamId(dto.steamId());
                l.setUserName(dto.userName());
                l.setRawValue(dto.rawValue());
                l.setMetal(dto.metal());
                l.setKeys(dto.keys() != null ? dto.keys() : 0L);
                l.setMarketName(dto.marketName());
                l.setDetails(dto.details());
                l.setListedAt(dto.listedAt());
                l.setUserPremium(dto.userPremium());
                l.setUserOnline(dto.userOnline());
                l.setUserBanned(dto.userBanned());
                l.setUserTradeOfferUrl(dto.userTradeOfferUrl());
                l.setTradeEvent(event);
                bptfSnap.add(l);
            }
        }
        event.setBptfListings(bptfSnap);
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
            List<TradeEventListItemDto.TradeItemDto> given = e.getItems().stream()
                    .filter(i -> "given".equals(i.getDirection()))
                    .map(i -> new TradeEventListItemDto.TradeItemDto(i.getSku(), i.getUsedPriceRefined()))
                    .toList();
            List<TradeEventListItemDto.TradeItemDto> received = e.getItems().stream()
                    .filter(i -> "received".equals(i.getDirection()))
                    .map(i -> new TradeEventListItemDto.TradeItemDto(i.getSku(), i.getUsedPriceRefined()))
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

    @Transactional(readOnly = true)
    public TradeEventDetailDto getDetail(long id) {
        TradeEvent e = eventRepo.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Trade " + id + " not found"));
        List<TradeEventDetailDto.TradeItemDto> given = e.getItems().stream()
                .filter(i -> "given".equals(i.getDirection()))
                .map(i -> new TradeEventDetailDto.TradeItemDto(i.getSku(), i.getUsedPriceRefined()))
                .toList();
        List<TradeEventDetailDto.TradeItemDto> received = e.getItems().stream()
                .filter(i -> "received".equals(i.getDirection()))
                .map(i -> new TradeEventDetailDto.TradeItemDto(i.getSku(), i.getUsedPriceRefined()))
                .toList();
        List<TradeEventDetailDto.SnapshotEntryDto> snapshot = e.getPricingSnapshot().stream()
                .map(s -> new TradeEventDetailDto.SnapshotEntryDto(
                        s.getSku(), s.getBuyPriceRefined(), s.getSellPriceRefined(), s.getIntent(),
                        s.isEnabled(), s.isAutoprice(), s.getMinStock(), s.getMaxStock(), s.isAutoManaged()))
                .toList();
        List<TradeEventDetailDto.BptfListingDto> listings = e.getBptfListings().stream()
                .map(l -> new TradeEventDetailDto.BptfListingDto(
                        l.getSku(), l.getIntent(),
                        l.getSteamId(), l.getUserName(),
                        l.getRawValue(), l.getMetal(), l.getKeys(),
                        l.getMarketName(), l.getDetails(), l.getListedAt(),
                        l.getUserPremium(), l.getUserOnline(), l.getUserBanned(),
                        l.getUserTradeOfferUrl()))
                .toList();
        return new TradeEventDetailDto(
                e.getId(), e.getOfferId(), e.getPartnerSteamId64(),
                e.getProfitInRefined(), e.getTradedAt().toString(),
                given, received, e.getStatus(), snapshot, listings);
    }
}
