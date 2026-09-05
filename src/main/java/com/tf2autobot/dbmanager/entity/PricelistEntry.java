package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pricelist")
@Getter @Setter @NoArgsConstructor
public class PricelistEntry {

    @Id
    @Column(name = "sku", length = 100)
    private String sku;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean autoprice = false;

    @Column(nullable = false)
    private int intent = 2;

    @Column(nullable = false)
    private int min = 0;

    @Column(nullable = false)
    private int max = 1;

    @Column(name = "buy_keys")
    private Integer buyKeys;

    @Column(name = "buy_metal", precision = 10, scale = 4)
    private BigDecimal buyMetal;

    @Column(name = "sell_keys")
    private Integer sellKeys;

    @Column(name = "sell_metal", precision = 10, scale = 4)
    private BigDecimal sellMetal;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "auto_managed", nullable = false)
    private boolean autoManaged = false;

    @Column(name = "integ_test_only", nullable = false)
    private boolean integTestOnly = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
