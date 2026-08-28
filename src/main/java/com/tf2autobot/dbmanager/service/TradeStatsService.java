package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.TradeStatsDto;
import com.tf2autobot.dbmanager.entity.TradeStats;
import com.tf2autobot.dbmanager.repository.TradeStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class TradeStatsService {

    private final TradeStatsRepository repo;

    public TradeStatsService(TradeStatsRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public TradeStatsDto get() {
        return TradeStatsDto.from(findSingleton());
    }

    public TradeStatsDto incrementAccepted(BigDecimal profitInRefined) {
        TradeStats stats = findSingleton();
        stats.setTradesAccepted(stats.getTradesAccepted() + 1);
        stats.setTotalProfitInRefined(stats.getTotalProfitInRefined().add(profitInRefined));
        return TradeStatsDto.from(repo.save(stats));
    }

    public TradeStatsDto incrementDeclined() {
        TradeStats stats = findSingleton();
        stats.setTradesDeclined(stats.getTradesDeclined() + 1);
        return TradeStatsDto.from(repo.save(stats));
    }

    public TradeStatsDto reset() {
        TradeStats stats = findSingleton();
        stats.setTradesAccepted(0);
        stats.setTradesDeclined(0);
        stats.setTotalProfitInRefined(BigDecimal.ZERO);
        return TradeStatsDto.from(repo.save(stats));
    }

    private TradeStats findSingleton() {
        return repo.findById(1).orElseThrow(() ->
                new IllegalStateException("trade_stats singleton row missing — check migration V3"));
    }
}
