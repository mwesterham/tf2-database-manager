package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "poll_data")
@Getter @Setter @NoArgsConstructor
public class PollDataEntry {

    @Id
    @Column(name = "offer_id", length = 50)
    private String offerId;

    @Column(nullable = false, length = 10)
    private String direction;

    @Column(nullable = false)
    private int state;

    @Column(name = "partner_steam_id64", length = 25)
    private String partnerSteamId64;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
