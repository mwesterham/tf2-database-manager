package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One sold unit paired with the unit it was bought as (see RealizedProfitService). */
@Entity
@Table(name = "trade_realized_pairs")
@Getter @Setter @NoArgsConstructor
public class TradeRealizedPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(name = "buy_trade_event_id", nullable = false)
    private long buyTradeEventId;

    @Column(name = "buy_item_id", nullable = false)
    private long buyItemId;

    @Column(name = "sell_trade_event_id", nullable = false)
    private long sellTradeEventId;

    @Column(name = "sell_item_id", nullable = false)
    private long sellItemId;

    @Column(name = "cost_refined", nullable = false)
    private double costRefined;

    @Column(name = "revenue_refined", nullable = false)
    private double revenueRefined;
}
