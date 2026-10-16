package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.DailyProfitDto;
import com.tf2autobot.dbmanager.dto.PurchaseDto;
import com.tf2autobot.dbmanager.dto.TradeEventDetailDto;
import com.tf2autobot.dbmanager.dto.TradeEventDto;
import com.tf2autobot.dbmanager.dto.TradeEventListItemDto;
import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.service.TradeEventService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trade-events")
public class TradeEventController {

    private final TradeEventService service;

    public TradeEventController(TradeEventService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> record(@RequestBody TradeEventDto dto) {
        service.record(dto.offerId(), dto.partnerSteamId64(), dto.profitInRefined(),
                dto.itemsGiven()      != null ? dto.itemsGiven()      : java.util.List.of(),
                dto.itemsReceived()   != null ? dto.itemsReceived()   : java.util.List.of(),
                dto.status(),
                dto.pricingSnapshot() != null ? dto.pricingSnapshot() : java.util.List.of(),
                dto.bptfListings()    != null ? dto.bptfListings()    : java.util.List.of(),
                dto.reason());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public Page<TradeEventListItemDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String partner,
            @RequestParam(required = false) String offerId,
            @RequestParam(required = false) Double minProfit,
            @RequestParam(required = false) Double maxProfit,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String item,
            @RequestParam(required = false) String status) {
        return service.list(page, size, sort, direction, partner, offerId, minProfit, maxProfit, dateFrom, dateTo, item, status);
    }

    /** Latest purchases (units received in accepted trades) per SKU; perSku capped at 50. */
    @GetMapping("/purchases")
    public List<PurchaseDto> purchases(@RequestParam List<String> skus,
                                       @RequestParam(defaultValue = "1") int perSku) {
        return service.getLatestPurchases(skus, Math.max(1, Math.min(perSku, 50)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeEventDetailDto> detail(@PathVariable long id) {
        try {
            return ResponseEntity.ok(service.getDetail(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/daily-profit")
    public List<DailyProfitDto> dailyProfit() {
        return service.getDailyProfit();
    }

    @GetMapping("/stats")
    public TradeEventStatsDto stats() {
        return service.getStats();
    }
}
