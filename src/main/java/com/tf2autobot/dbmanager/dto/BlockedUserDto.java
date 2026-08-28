package com.tf2autobot.dbmanager.dto;

import com.tf2autobot.dbmanager.entity.BlockedUser;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record BlockedUserDto(
        @NotBlank String steamId64,
        String reason,
        Instant blockedAt
) {
    public static BlockedUserDto from(BlockedUser u) {
        return new BlockedUserDto(u.getSteamId64(), u.getReason(), u.getBlockedAt());
    }
}
