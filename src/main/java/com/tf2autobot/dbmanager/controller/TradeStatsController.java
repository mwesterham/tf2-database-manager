package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.TradeStatsDto;
import com.tf2autobot.dbmanager.service.TradeStatsService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/trade-stats")
public class TradeStatsController {

    private final TradeStatsService service;

    public TradeStatsController(TradeStatsService service) {
        this.service = service;
    }

    @GetMapping
    public TradeStatsDto get() {
        return service.get();
    }

    @PostMapping("/accepted")
    public TradeStatsDto accepted(@RequestBody(required = false) Map<String, Object> body) {
        BigDecimal profit = BigDecimal.ZERO;
        if (body != null && body.get("profitInRefined") != null) {
            profit = new BigDecimal(body.get("profitInRefined").toString());
        }
        return service.incrementAccepted(profit);
    }

    @PostMapping("/declined")
    public TradeStatsDto declined() {
        return service.incrementDeclined();
    }

    @PostMapping("/reset")
    public TradeStatsDto reset() {
        return service.reset();
    }
}
