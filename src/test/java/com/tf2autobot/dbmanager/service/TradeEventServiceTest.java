package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.entity.TradeEvent;
import com.tf2autobot.dbmanager.repository.TradeEventItemRepository;
import com.tf2autobot.dbmanager.repository.TradeEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TradeEventServiceTest {

    private TradeEventRepository eventRepo;
    private TradeEventItemRepository itemRepo;
    private TradeEventService service;

    @BeforeEach
    void setUp() {
        eventRepo = mock(TradeEventRepository.class);
        itemRepo = mock(TradeEventItemRepository.class);
        service = new TradeEventService(eventRepo, itemRepo);
    }

    @Test
    void recordSavesEvent() {
        when(eventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.record("offer-1", "76561198000000000", 1.33,
                List.of("5021;6"), List.of("30;6"));
        verify(eventRepo).save(any(TradeEvent.class));
    }

    @Test
    void recordStoresItemsWithCorrectDirections() {
        when(eventRepo.save(any())).thenAnswer(inv -> {
            TradeEvent e = inv.getArgument(0);
            assertEquals(2, e.getItems().size());
            assertEquals("5021;6", e.getItems().get(0).getSku());
            assertEquals("given", e.getItems().get(0).getDirection());
            assertEquals("30;6", e.getItems().get(1).getSku());
            assertEquals("received", e.getItems().get(1).getDirection());
            return e;
        });
        service.record("offer-1", "76561198000000001", 2.0,
                List.of("5021;6"), List.of("30;6"));
    }

    @Test
    void recordHandlesEmptyItemLists() {
        when(eventRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertDoesNotThrow(() -> service.record("offer-2", "76561198000000000", 0.0,
                List.of(), List.of()));
    }

    @Test
    void getStatsReturnsAggregatedData() {
        when(eventRepo.count()).thenReturn(5L);
        when(eventRepo.sumProfit()).thenReturn(Optional.of(10.5));
        List<Object[]> traded = new java.util.ArrayList<>();
        traded.add(new Object[]{"5021;6", 3L});
        when(itemRepo.findMostTradedItems()).thenReturn(traded);

        List<Object[]> profitable = new java.util.ArrayList<>();
        profitable.add(new Object[]{"30;6", 8.5});
        when(itemRepo.findMostProfitableItems()).thenReturn(profitable);

        TradeEventStatsDto stats = service.getStats();

        assertEquals(5L, stats.totalTrades());
        assertEquals(10.5, stats.totalProfitInRefined(), 1e-9);
        assertEquals(1, stats.mostTradedItems().size());
        assertEquals("5021;6", stats.mostTradedItems().get(0).sku());
        assertEquals(3L, stats.mostTradedItems().get(0).tradeCount());
        assertEquals(1, stats.mostProfitableItems().size());
        assertEquals("30;6", stats.mostProfitableItems().get(0).sku());
        assertEquals(8.5, stats.mostProfitableItems().get(0).totalProfitInRefined(), 1e-9);
    }

    @Test
    void getStatsHandlesNoTrades() {
        when(eventRepo.count()).thenReturn(0L);
        when(eventRepo.sumProfit()).thenReturn(Optional.empty());
        when(itemRepo.findMostTradedItems()).thenReturn(List.of());
        when(itemRepo.findMostProfitableItems()).thenReturn(List.of());

        TradeEventStatsDto stats = service.getStats();

        assertEquals(0L, stats.totalTrades());
        assertEquals(0.0, stats.totalProfitInRefined(), 1e-9);
        assertTrue(stats.mostTradedItems().isEmpty());
        assertTrue(stats.mostProfitableItems().isEmpty());
    }
}
