package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.BlockedUserDto;
import com.tf2autobot.dbmanager.entity.BlockedUser;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.BlockedUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BlockedUserService {

    private final BlockedUserRepository repo;

    public BlockedUserService(BlockedUserRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<BlockedUserDto> getAll() {
        return repo.findAll().stream().map(BlockedUserDto::from).toList();
    }

    @Transactional(readOnly = true)
    public BlockedUserDto getBySteamId(String steamId64) {
        return BlockedUserDto.from(findOrThrow(steamId64));
    }

    public BlockedUserDto block(String steamId64, String reason) {
        BlockedUser user = repo.findById(steamId64).orElseGet(() -> {
            BlockedUser u = new BlockedUser();
            u.setSteamId64(steamId64);
            return u;
        });
        user.setReason(reason);
        return BlockedUserDto.from(repo.save(user));
    }

    public void unblock(String steamId64) {
        if (!repo.existsById(steamId64)) throw new NotFoundException("User not found: " + steamId64);
        repo.deleteById(steamId64);
    }

    private BlockedUser findOrThrow(String steamId64) {
        return repo.findById(steamId64)
                .orElseThrow(() -> new NotFoundException("User not found: " + steamId64));
    }
}
