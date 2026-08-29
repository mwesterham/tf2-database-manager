package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.PricelistEntryDto;
import com.tf2autobot.dbmanager.service.PricelistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pricelist")
public class PricelistController {

    private final PricelistService service;

    public PricelistController(PricelistService service) {
        this.service = service;
    }

    @GetMapping
    public List<PricelistEntryDto> getAll(@RequestParam(required = false) Boolean enabled) {
        if (Boolean.TRUE.equals(enabled)) return service.getEnabled();
        return service.getAll();
    }

    @GetMapping("/{sku:.+}")
    public PricelistEntryDto getBySku(@PathVariable String sku) {
        return service.getBySku(sku);
    }

    @PutMapping("/{sku:.+}")
    public PricelistEntryDto upsert(@PathVariable String sku, @Valid @RequestBody PricelistEntryDto dto) {
        // Ensure the path sku matches the body sku by reconstructing with path sku
        PricelistEntryDto normalized = new PricelistEntryDto(
                sku, dto.enabled(), dto.autoprice(), dto.intent(),
                dto.min(), dto.max(), dto.buy(), dto.sell(), dto.note(), dto.autoManaged(),
                dto.createdAt(), dto.updatedAt()
        );
        return service.upsert(normalized);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PricelistEntryDto create(@Valid @RequestBody PricelistEntryDto dto) {
        return service.upsert(dto);
    }

    @DeleteMapping("/{sku:.+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String sku) {
        service.delete(sku);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        service.deleteAll();
    }
}
