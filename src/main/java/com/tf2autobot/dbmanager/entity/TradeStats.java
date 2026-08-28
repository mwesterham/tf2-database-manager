package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade_stats")
@Getter @Setter @NoArgsConstructor
public class TradeStats {

    @Id
    @Column(nullable = false)
    private int id = 1;

    @Column(nullable = false)
    private Instant since = Instant.now();

    @Column(name = "trades_accepted", nullable = false)
    private int tradesAccepted = 0;

    @Column(name = "trades_declined", nullable = false)
    private int tradesDeclined = 0;

    @Column(name = "total_profit_in_refined", nullable = false, precision = 12, scale = 4)
    private BigDecimal totalProfitInRefined = BigDecimal.ZERO;
}
