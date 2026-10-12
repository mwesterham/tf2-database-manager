package com.tf2autobot.dbmanager.controller;

import com.tf2autobot.dbmanager.entity.Setting;
import com.tf2autobot.dbmanager.repository.SettingRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class SettingsController {

    private final SettingRepository repo;

    public SettingsController(SettingRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/{key}")
    public ResponseEntity<String> get(@PathVariable String key) {
        return repo.findById(key)
                .map(s -> ResponseEntity.ok(s.getValue()))
                .orElse(ResponseEntity.notFound().<String>build());
    }

    @PutMapping("/{key}")
    public ResponseEntity<Void> put(@PathVariable String key, @RequestBody String value) {
        repo.save(new Setting(key, value));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> delete(@PathVariable String key) {
        repo.deleteById(key);
        return ResponseEntity.ok().build();
    }
}
