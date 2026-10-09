package com.tf2autobot.dbmanager.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class SteamUserService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public Set<String> findIdsByName(String nameQuery) {
        String lower = nameQuery.toLowerCase();
        return cache.entrySet().stream()
                .filter(e -> e.getValue().toLowerCase().contains(lower))
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Map<String, String> getUserNames(Collection<String> steamIds) {
        Map<String, String> result = new HashMap<>();
        for (String id : steamIds) {
            result.put(id, cache.computeIfAbsent(id, this::fetchSafely));
        }
        return result;
    }

    private String fetchSafely(String steamId) {
        try {
            String xml = RestClient.create()
                    .get()
                    .uri("https://steamcommunity.com/profiles/" + steamId + "/?xml=1")
                    .retrieve()
                    .body(String.class);
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            var nodes = doc.getElementsByTagName("steamID");
            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().trim();
            }
        } catch (Exception e) {
            log.warn("Steam name lookup failed for {}: {}", steamId, e.getMessage());
        }
        return steamId;
    }
}
