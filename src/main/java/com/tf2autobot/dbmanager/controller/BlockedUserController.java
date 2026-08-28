package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.dto.BlockedUserDto;
import com.tf2autobot.dbmanager.service.BlockedUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blocked-users")
public class BlockedUserController {

    private final BlockedUserService service;

    public BlockedUserController(BlockedUserService service) {
        this.service = service;
    }

    @GetMapping
    public List<BlockedUserDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{steamId64}")
    public BlockedUserDto get(@PathVariable String steamId64) {
        return service.getBySteamId(steamId64);
    }

    @PutMapping("/{steamId64}")
    public BlockedUserDto block(@PathVariable String steamId64,
                                @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return service.block(steamId64, reason);
    }

    @DeleteMapping("/{steamId64}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable String steamId64) {
        service.unblock(steamId64);
    }
}
