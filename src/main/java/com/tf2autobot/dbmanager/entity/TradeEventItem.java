package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trade_event_items")
@Getter @Setter @NoArgsConstructor
public class TradeEventItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_event_id", nullable = false)
    private TradeEvent tradeEvent;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String direction; // "given" or "received"
}
