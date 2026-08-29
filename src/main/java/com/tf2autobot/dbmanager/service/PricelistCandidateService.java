package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.client.PricerClient;
import com.tf2autobot.dbmanager.dto.CandidateItemDto;
import com.tf2autobot.dbmanager.entity.PricelistEntry;
import com.tf2autobot.dbmanager.repository.PricelistRepository;
import com.tf2autobot.dbmanager.repository.TradeEventItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricelistCandidateService {

    private final PricerClient pricerClient;
    private final PricelistRepository pricelistRepository;
    private final TradeEventItemRepository tradeEventItemRepository;

    @Value("${app.candidate.max-auto-managed:30}")
    private int maxAutoManaged;

    @Value("${app.candidate.fetch-limit:50}")
    private int fetchLimit;

    @Transactional
    public void syncCandidates() {
        log.info("Starting pricelist candidate sync");

        List<CandidateItemDto> candidates = pricerClient.getCandidates(fetchLimit);
        if (candidates.isEmpty()) {
            log.warn("Pricer returned no candidates — skipping sync");
            return;
        }

        Set<String> candidateSkus = candidates.stream()
            .map(CandidateItemDto::sku)
            .collect(Collectors.toSet());

        Set<String> existingSkus = pricelistRepository.findAll().stream()
            .map(PricelistEntry::getSku)
            .collect(Collectors.toSet());

        Set<String> heldSkus = Set.copyOf(tradeEventItemRepository.findHeldItemSkus());

        // Remove auto-managed entries that fell off the candidate list and are not held
        List<PricelistEntry> autoManaged = pricelistRepository.findAllByAutoManaged(true);
        List<String> toRemove = autoManaged.stream()
            .map(PricelistEntry::getSku)
            .filter(sku -> !candidateSkus.contains(sku) && !heldSkus.contains(sku))
            .toList();

        if (!toRemove.isEmpty()) {
            log.info("Removing {} auto-managed items that dropped off the candidate list: {}", toRemove.size(), toRemove);
            toRemove.forEach(pricelistRepository::deleteById);
        }

        // Add top candidates not already in the pricelist, up to the cap
        long currentAutoCount = pricelistRepository.findAllByAutoManaged(true).size();
        int slots = maxAutoManaged - (int) currentAutoCount;

        if (slots <= 0) {
            log.info("Auto-managed cap ({}) reached — no new items will be added", maxAutoManaged);
            return;
        }

        List<CandidateItemDto> toAdd = candidates.stream()
            .filter(c -> !existingSkus.contains(c.sku()))
            .limit(slots)
            .toList();

        if (toAdd.isEmpty()) {
            log.info("No new candidates to add");
            return;
        }

        log.info("Adding {} new auto-managed pricelist entries", toAdd.size());
        for (CandidateItemDto candidate : toAdd) {
            PricelistEntry entry = new PricelistEntry();
            entry.setSku(candidate.sku());
            entry.setEnabled(true);
            entry.setAutoprice(true);
            entry.setIntent(2); // buy and sell
            entry.setMin(0);
            entry.setMax(1);
            entry.setAutoManaged(true);
            entry.setNote("auto");
            entry.setCreatedAt(Instant.now());
            entry.setBuyKeys(0);
            entry.setBuyMetal(BigDecimal.valueOf(candidate.bestBuyMetal()));
            entry.setSellKeys(0);
            entry.setSellMetal(BigDecimal.valueOf(candidate.bestSellMetal()));
            pricelistRepository.save(entry);
            log.info("Added auto-managed entry: {} ({})", candidate.sku(), candidate.marketName());
        }

        log.info("Candidate sync complete — removed={}, added={}", toRemove.size(), toAdd.size());
    }
}
