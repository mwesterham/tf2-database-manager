package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trade_event_bptf_listings")
@Getter @Setter @NoArgsConstructor
public class TradeEventBptfListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_event_id", nullable = false)
    private TradeEvent tradeEvent;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String intent;

    @Column(nullable = false)
    private int keys;

    @Column(name = "half_scrap", nullable = false)
    private long halfScrap;

    @Column(nullable = false)
    private int stock;

    @Column(name = "listing_limit", nullable = false)
    private int listingLimit;
}
