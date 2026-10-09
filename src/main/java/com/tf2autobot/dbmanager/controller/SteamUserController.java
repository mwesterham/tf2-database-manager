package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.service.SteamUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/steam")
public class SteamUserController {

    private final SteamUserService steamUserService;

    public SteamUserController(SteamUserService steamUserService) {
        this.steamUserService = steamUserService;
    }

    @GetMapping("/user-names")
    public Map<String, String> userNames(@RequestParam List<String> steamids) {
        return steamUserService.getUserNames(steamids);
    }
}
