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

    @Column(name = "steam_id")
    private String steamId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "raw_value")
    private Double rawValue;

    @Column
    private Double metal;

    @Column(nullable = false)
    private long keys;

    @Column(name = "market_name")
    private String marketName;

    @Column
    private String details;

    @Column(name = "listed_at")
    private Long listedAt;

    @Column(name = "user_premium")
    private Boolean userPremium;

    @Column(name = "user_online")
    private Boolean userOnline;

    @Column(name = "user_banned")
    private Boolean userBanned;

    @Column(name = "user_trade_offer_url")
    private String userTradeOfferUrl;
}
