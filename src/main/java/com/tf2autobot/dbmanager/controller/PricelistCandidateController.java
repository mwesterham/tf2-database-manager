package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.service.PricelistCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pricelist/candidates")
@RequiredArgsConstructor
@Slf4j
public class PricelistCandidateController {

    private final PricelistCandidateService candidateService;

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sync() {
        log.info("Pricelist candidate sync requested via API");
        CompletableFuture.runAsync(candidateService::syncCandidates);
    }
}
