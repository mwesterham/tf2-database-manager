package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.TradeStatsDto;
import com.tf2autobot.dbmanager.entity.TradeStats;
import com.tf2autobot.dbmanager.repository.TradeStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeStatsServiceTest {

    @Mock
    private TradeStatsRepository repo;

    @InjectMocks
    private TradeStatsService service;

    private TradeStats stats;

    @BeforeEach
    void setUp() {
        stats = new TradeStats();
        stats.setId(1);
        stats.setTradesAccepted(5);
        stats.setTradesDeclined(2);
        stats.setTotalProfitInRefined(new BigDecimal("3.33"));
    }

    @Test
    void get_returnsSingleton() {
        when(repo.findById(1)).thenReturn(Optional.of(stats));
        TradeStatsDto dto = service.get();
        assertThat(dto.tradesAccepted()).isEqualTo(5);
        assertThat(dto.tradesDeclined()).isEqualTo(2);
    }

    @Test
    void incrementAccepted_updatesCountAndProfit() {
        when(repo.findById(1)).thenReturn(Optional.of(stats));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TradeStatsDto result = service.incrementAccepted(new BigDecimal("1.00"));
        assertThat(result.tradesAccepted()).isEqualTo(6);
        assertThat(result.totalProfitInRefined()).isEqualByComparingTo("4.33");
    }

    @Test
    void incrementDeclined_updatesCount() {
        when(repo.findById(1)).thenReturn(Optional.of(stats));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TradeStatsDto result = service.incrementDeclined();
        assertThat(result.tradesDeclined()).isEqualTo(3);
    }

    @Test
    void reset_clearsAll() {
        when(repo.findById(1)).thenReturn(Optional.of(stats));
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TradeStatsDto result = service.reset();
        assertThat(result.tradesAccepted()).isZero();
        assertThat(result.tradesDeclined()).isZero();
        assertThat(result.totalProfitInRefined()).isEqualByComparingTo("0");
    }

    @Test
    void get_missingSingleton_throws() {
        when(repo.findById(1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get())
                .isInstanceOf(IllegalStateException.class);
    }
}
