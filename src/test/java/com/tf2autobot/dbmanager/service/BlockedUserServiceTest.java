package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.BlockedUserDto;
import com.tf2autobot.dbmanager.entity.BlockedUser;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.BlockedUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockedUserServiceTest {

    @Mock
    private BlockedUserRepository repo;

    @InjectMocks
    private BlockedUserService service;

    @Test
    void getAll_returnsMappedDtos() {
        BlockedUser u = new BlockedUser();
        u.setSteamId64("76561198000000001");
        u.setReason("scammer");
        when(repo.findAll()).thenReturn(List.of(u));

        List<BlockedUserDto> result = service.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).steamId64()).isEqualTo("76561198000000001");
    }

    @Test
    void block_newUser() {
        when(repo.findById("76561198000000001")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BlockedUserDto dto = service.block("76561198000000001", "cheater");
        assertThat(dto.steamId64()).isEqualTo("76561198000000001");
        assertThat(dto.reason()).isEqualTo("cheater");
    }

    @Test
    void unblock_notFound_throws() {
        when(repo.existsById("76561198000000001")).thenReturn(false);
        assertThatThrownBy(() -> service.unblock("76561198000000001"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unblock_existing() {
        when(repo.existsById("76561198000000001")).thenReturn(true);
        service.unblock("76561198000000001");
        verify(repo).deleteById("76561198000000001");
    }
}
