package com.tf2autobot.dbmanager.dto;

import com.tf2autobot.dbmanager.entity.PollDataEntry;

import java.time.Instant;
import java.util.List;

public record PollDataDto(List<PollDataEntryDto> entries) {

    public record PollDataEntryDto(
            String offerId,
            String direction,
            int state,
            String partnerSteamId64,
            Instant updatedAt
    ) {
        public static PollDataEntryDto from(PollDataEntry e) {
            return new PollDataEntryDto(
                    e.getOfferId(), e.getDirection(), e.getState(),
                    e.getPartnerSteamId64(), e.getUpdatedAt()
            );
        }
    }
}
