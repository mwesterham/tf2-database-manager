package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trade_event_snapshots")
@Getter @Setter @NoArgsConstructor
public class TradeEventSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_event_id", nullable = false)
    private TradeEvent tradeEvent;

    @Column(nullable = false)
    private String sku;

    @Column(name = "buy_price_refined", nullable = false)
    private double buyPriceRefined;

    @Column(name = "sell_price_refined", nullable = false)
    private double sellPriceRefined;

    @Column(nullable = false)
    private String intent;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean autoprice;

    @Column(name = "min_stock", nullable = false)
    private int minStock;

    @Column(name = "max_stock", nullable = false)
    private int maxStock;

    @Column(name = "auto_managed", nullable = false)
    private boolean autoManaged;
}
