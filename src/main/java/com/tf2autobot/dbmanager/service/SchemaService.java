package com.tf2autobot.dbmanager.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchemaService {

    private static final String SCHEMA_URL = "https://schema.autobot.tf/schema";

    private final ObjectMapper mapper;

    private volatile Map<Integer, String> defindexToName = Map.of();

    @PostConstruct
    public void init() {
        try {
            String json = RestClient.create()
                    .get()
                    .uri(SCHEMA_URL)
                    .retrieve()
                    .body(String.class);
            loadSchema(json);
            log.info("TF2 schema loaded: {} items", defindexToName.size());
        } catch (Exception e) {
            log.warn("Failed to load TF2 schema: {}", e.getMessage());
        }
    }

    void loadSchema(String json) throws Exception {
        JsonNode schema = mapper.readTree(json).path("raw").path("schema");

        Map<Integer, String> items = new HashMap<>();
        for (JsonNode item : schema.path("items")) {
            int defindex = item.path("defindex").asInt(-1);
            String name = item.path("item_name").asText("");
            if (defindex > 0 && !name.isEmpty()) {
                items.put(defindex, name);
            }
        }

        defindexToName = Collections.unmodifiableMap(items);
    }

    public Set<Integer> findDefindexesByName(String query) {
        String lower = query.toLowerCase();
        return defindexToName.entrySet().stream()
                .filter(e -> e.getValue().toLowerCase().contains(lower))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Map<String, String> resolveNames(Collection<String> skus) {
        Map<String, String> result = new HashMap<>();
        for (String sku : skus) {
            result.put(sku, resolveName(sku));
        }
        return result;
    }

    private String resolveName(String sku) {
        String[] parts = sku.split(";");
        if (parts.length < 1) return sku;
        try {
            int defindex = Integer.parseInt(parts[0]);
            return defindexToName.getOrDefault(defindex, sku);
        } catch (NumberFormatException e) {
            return sku;
        }
    }
}
