package com.tf2autobot.dbmanager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@RestController
@RequestMapping("/steam")
public class SteamProxyController {

    private final String steamApiKey;
    private final HttpClient http = HttpClient.newHttpClient();

    public SteamProxyController(@Value("${STEAM_WEB_API_KEY}") String steamApiKey) {
        this.steamApiKey = steamApiKey;
    }

    @GetMapping(value = "/trade-offer/{offerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getTradeOffer(@PathVariable String offerId) {
        try {
            String url = "https://api.steampowered.com/IEconService/GetTradeOffer/v1"
                    + "?key=" + steamApiKey
                    + "&tradeofferid=" + offerId
                    + "&language=en"
                    + "&get_descriptions=1";
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(resp.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp.body());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @GetMapping(value = "/asset-class-info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAssetClassInfo(@RequestParam Map<String, String> params) {
        try {
            StringBuilder url = new StringBuilder(
                    "https://api.steampowered.com/ISteamEconomy/GetAssetClassInfo/v1/?key=" + steamApiKey + "&language=en");
            for (Map.Entry<String, String> e : params.entrySet()) {
                url.append("&").append(e.getKey()).append("=").append(e.getValue());
            }
            HttpRequest req = HttpRequest.newBuilder(URI.create(url.toString())).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            return ResponseEntity.status(resp.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp.body());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
