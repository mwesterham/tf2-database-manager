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
    private volatile long loadedAtMs = 0;

    static final long RETRY_EVERY_MS = 60_000L;
    static final long REFRESH_AFTER_MS = 24 * 3_600_000L;

    // Swappable for tests
    java.util.function.Supplier<String> fetcher = SchemaService::download;
    java.util.function.LongSupplier clock = System::currentTimeMillis;

    private final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "schema-refresh");
                t.setDaemon(true);
                return t;
            });

    /**
     * Loads the schema at startup, then checks every minute: download again while it isn't loaded (on 2026-10-31
     * one failed startup download left every item name blank until the next restart) or once it's a day old.
     */
    @PostConstruct
    public void init() {
        refreshIfNeeded();
        scheduler.scheduleWithFixedDelay(this::refreshIfNeeded, RETRY_EVERY_MS, RETRY_EVERY_MS,
                java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /** Downloads the schema if it isn't loaded or is over a day old. A failure keeps the names we have. */
    void refreshIfNeeded() {
        long now = clock.getAsLong();
        boolean loaded = !defindexToName.isEmpty();
        if (loaded && now - loadedAtMs < REFRESH_AFTER_MS) return;
        try {
            loadSchema(fetcher.get());
            loadedAtMs = now;
            log.info("TF2 schema loaded: {} items", defindexToName.size());
        } catch (Exception e) {
            log.warn("Failed to load TF2 schema{}: {}", loaded ? " (keeping the previous one)" : " — retrying every minute",
                    e.getMessage());
        }
    }

    private static String download() {
        return RestClient.create().get().uri(SCHEMA_URL).retrieve().body(String.class);
    }

    boolean isLoaded() {
        return !defindexToName.isEmpty();
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
