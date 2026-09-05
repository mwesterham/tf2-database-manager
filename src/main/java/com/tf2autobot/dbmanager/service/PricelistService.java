package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.CurrenciesDto;
import com.tf2autobot.dbmanager.dto.PricelistEntryDto;
import com.tf2autobot.dbmanager.entity.PricelistEntry;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.PricelistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PricelistService {

    private final PricelistRepository repo;

    public PricelistService(PricelistRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<PricelistEntryDto> getAll() {
        return repo.findAll().stream().map(PricelistEntryDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PricelistEntryDto> getEnabled() {
        return repo.findAllByEnabled(true).stream().map(PricelistEntryDto::from).toList();
    }

    @Transactional(readOnly = true)
    public PricelistEntryDto getBySku(String sku) {
        return PricelistEntryDto.from(findOrThrow(sku));
    }

    public PricelistEntryDto upsert(PricelistEntryDto dto) {
        PricelistEntry entry = repo.findById(dto.sku()).orElseGet(() -> {
            PricelistEntry e = new PricelistEntry();
            e.setSku(dto.sku());
            e.setCreatedAt(Instant.now());
            return e;
        });
        applyDto(entry, dto);
        return PricelistEntryDto.from(repo.save(entry));
    }

    public void delete(String sku) {
        if (!repo.existsById(sku)) throw new NotFoundException("SKU not found: " + sku);
        repo.deleteById(sku);
    }

    public void deleteAll() {
        repo.deleteAll();
    }

    private void applyDto(PricelistEntry e, PricelistEntryDto dto) {
        e.setEnabled(dto.enabled());
        e.setAutoprice(dto.autoprice());
        e.setIntent(dto.intent());
        e.setMin(dto.min());
        e.setMax(dto.max());
        e.setNote(dto.note());
        e.setAutoManaged(dto.autoManaged());
        e.setIntegTestOnly(dto.integTestOnly());

        CurrenciesDto buy = dto.buy();
        if (buy != null) {
            e.setBuyKeys(buy.keys());
            e.setBuyMetal(buy.metal());
        } else {
            e.setBuyKeys(null);
            e.setBuyMetal(null);
        }

        CurrenciesDto sell = dto.sell();
        if (sell != null) {
            e.setSellKeys(sell.keys());
            e.setSellMetal(sell.metal());
        } else {
            e.setSellKeys(null);
            e.setSellMetal(null);
        }
    }

    private PricelistEntry findOrThrow(String sku) {
        return repo.findById(sku).orElseThrow(() -> new NotFoundException("SKU not found: " + sku));
    }
}
