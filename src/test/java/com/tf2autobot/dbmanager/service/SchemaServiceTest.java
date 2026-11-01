package com.tf2autobot.dbmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SchemaServiceTest {

    private static final String SCHEMA = """
            {"raw":{"schema":{"items":[{"defindex":31656,"item_name":"Scaled Surcoat"},
                                       {"defindex":5021,"item_name":"Mann Co. Supply Crate Key"}]}}}
            """;
    private static final String SCHEMA_V2 = """
            {"raw":{"schema":{"items":[{"defindex":31656,"item_name":"Scaled Surcoat (renamed)"}]}}}
            """;

    private SchemaService service(long[] now, AtomicInteger calls, String... responses) {
        SchemaService s = new SchemaService(new ObjectMapper());
        s.clock = () -> now[0];
        s.fetcher = () -> {
            int i = calls.getAndIncrement();
            String r = responses[Math.min(i, responses.length - 1)];
            if (r == null) throw new RuntimeException("I/O error on GET request");
            return r;
        };
        return s;
    }

    @Test
    void aFailedStartupDownloadIsRetriedUntilItLoads() {
        long[] now = {1_000_000L};
        AtomicInteger calls = new AtomicInteger();
        SchemaService s = service(now, calls, null, null, SCHEMA);

        s.refreshIfNeeded();                       // startup: fails
        assertFalse(s.isLoaded());
        assertEquals("31656;6", s.resolveNames(List.of("31656;6")).get("31656;6"));   // falls back to the SKU
        now[0] += SchemaService.RETRY_EVERY_MS;
        s.refreshIfNeeded();                       // fails again
        now[0] += SchemaService.RETRY_EVERY_MS;
        s.refreshIfNeeded();                       // loads

        assertTrue(s.isLoaded());
        assertEquals("Scaled Surcoat", s.resolveNames(List.of("31656;6")).get("31656;6"));
        assertEquals(3, calls.get());
    }

    @Test
    void aLoadedSchemaIsOnlyRefreshedAfterADayAndAFailedRefreshKeepsIt() {
        long[] now = {1_000_000L};
        AtomicInteger calls = new AtomicInteger();
        SchemaService s = service(now, calls, SCHEMA, null, SCHEMA_V2);

        s.refreshIfNeeded();
        now[0] += 60 * 60_000L;
        s.refreshIfNeeded();                       // an hour later: no download
        assertEquals(1, calls.get());

        now[0] += SchemaService.REFRESH_AFTER_MS;
        s.refreshIfNeeded();                       // a day later: download fails, old names kept
        assertEquals("Scaled Surcoat", s.resolveNames(List.of("31656;6")).get("31656;6"));
        s.refreshIfNeeded();                       // next check: loads the new one
        assertEquals("Scaled Surcoat (renamed)", s.resolveNames(List.of("31656;6")).get("31656;6"));
    }
}
