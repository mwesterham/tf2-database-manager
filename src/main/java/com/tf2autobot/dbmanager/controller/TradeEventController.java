package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.TradeEventDto;
import com.tf2autobot.dbmanager.dto.TradeEventStatsDto;
import com.tf2autobot.dbmanager.service.TradeEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                dto.itemsGiven() != null ? dto.itemsGiven() : java.util.List.of(),
                dto.itemsReceived() != null ? dto.itemsReceived() : java.util.List.of());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public TradeEventStatsDto stats() {
        return service.getStats();
    }
}
