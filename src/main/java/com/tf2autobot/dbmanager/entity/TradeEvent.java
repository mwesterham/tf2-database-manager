package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade_events")
@Getter @Setter @NoArgsConstructor
public class TradeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(name = "partner_steam_id64")
    private String partnerSteamId64;

    @Column(name = "profit_in_refined", nullable = false)
    private double profitInRefined;

    @Column(name = "traded_at", nullable = false)
    private Instant tradedAt = Instant.now();

    @OneToMany(mappedBy = "tradeEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeEventItem> items = new ArrayList<>();
}
