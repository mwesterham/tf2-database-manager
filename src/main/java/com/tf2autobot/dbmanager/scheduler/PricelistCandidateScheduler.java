package com.tf2autobot.dbmanager.scheduler;

import com.tf2autobot.dbmanager.service.PricelistCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PricelistCandidateScheduler {

    private final PricelistCandidateService candidateService;

    @Scheduled(cron = "${app.candidate.schedule:0 0 */6 * * *}")
    public void run() {
        log.info("Pricelist candidate scheduler triggered");
        try {
            candidateService.syncCandidates();
        } catch (Exception e) {
            log.error("Pricelist candidate sync failed", e);
        }
    }
}
