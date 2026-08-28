package com.tf2autobot.dbmanager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "blocked_users")
@Getter @Setter @NoArgsConstructor
public class BlockedUser {

    @Id
    @Column(name = "steam_id64", length = 25)
    private String steamId64;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "blocked_at", nullable = false)
    private Instant blockedAt = Instant.now();
}
