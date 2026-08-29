package com.tf2autobot.dbmanager.client;

import com.tf2autobot.dbmanager.dto.CandidateItemDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@Slf4j
public class PricerClient {

    private final RestTemplate restTemplate;
    private final String pricerBaseUrl;

    public PricerClient(
            RestTemplate restTemplate,
            @Value("${app.pricer.base-url}") String pricerBaseUrl) {
        this.restTemplate = restTemplate;
        this.pricerBaseUrl = pricerBaseUrl;
    }

    public List<CandidateItemDto> getCandidates(int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(pricerBaseUrl)
            .path("/items/candidates")
            .queryParam("limit", limit)
            .toUriString();

        log.info("Fetching candidates from pricer: {}", url);
        List<CandidateItemDto> candidates = restTemplate.exchange(
            url, HttpMethod.GET, null,
            new ParameterizedTypeReference<List<CandidateItemDto>>() {}
        ).getBody();

        int count = candidates != null ? candidates.size() : 0;
        log.info("Received {} candidates from pricer", count);
        return candidates != null ? candidates : List.of();
    }
}
