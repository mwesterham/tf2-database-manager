package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.PollDataDto;
import com.tf2autobot.dbmanager.service.PollDataService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/poll-data")
public class PollDataController {

    private final PollDataService service;

    public PollDataController(PollDataService service) {
        this.service = service;
    }

    @GetMapping
    public PollDataDto getAll() {
        return service.getAll();
    }

    @GetMapping("/settings")
    public Map<String, String> getSettings() {
        return service.getSettings();
    }

    @GetMapping("/offer-since")
    public Map<String, String> getOfferSince() {
        return Map.of("offerSince", service.getOfferSince());
    }

    @PutMapping("/offer-since")
    public Map<String, String> setOfferSince(@RequestBody Map<String, String> body) {
        String ts = body.get("offerSince");
        if (ts == null) throw new IllegalArgumentException("offerSince is required");
        service.setOfferSince(ts);
        return Map.of("offerSince", ts);
    }

    @PutMapping("/{offerId}")
    public void upsertEntry(@PathVariable String offerId,
                            @RequestBody Map<String, Object> body) {
        String direction = (String) body.get("direction");
        int state = ((Number) body.get("state")).intValue();
        String partnerSteamId64 = (String) body.get("partnerSteamId64");
        service.upsertEntry(offerId, direction, state, partnerSteamId64);
    }

    @DeleteMapping("/{offerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable String offerId) {
        service.deleteEntry(offerId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAll() {
        service.clearAll();
    }
}
