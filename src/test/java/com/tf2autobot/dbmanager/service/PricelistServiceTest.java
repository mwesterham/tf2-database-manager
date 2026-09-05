package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.CurrenciesDto;
import com.tf2autobot.dbmanager.dto.PricelistEntryDto;
import com.tf2autobot.dbmanager.entity.PricelistEntry;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.PricelistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricelistServiceTest {

    @Mock
    private PricelistRepository repo;

    @InjectMocks
    private PricelistService service;

    private PricelistEntry sampleEntry;

    @BeforeEach
    void setUp() {
        sampleEntry = new PricelistEntry();
        sampleEntry.setSku("5021;6");
        sampleEntry.setEnabled(true);
        sampleEntry.setAutoprice(false);
        sampleEntry.setIntent(2);
        sampleEntry.setMin(0);
        sampleEntry.setMax(1);
        sampleEntry.setBuyKeys(1);
        sampleEntry.setBuyMetal(new BigDecimal("0.33"));
        sampleEntry.setSellKeys(1);
        sampleEntry.setSellMetal(new BigDecimal("0.66"));
    }

    @Test
    void getAll_returnsMappedDtos() {
        when(repo.findAll()).thenReturn(List.of(sampleEntry));
        List<PricelistEntryDto> result = service.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).sku()).isEqualTo("5021;6");
    }

    @Test
    void getBySku_found() {
        when(repo.findById("5021;6")).thenReturn(Optional.of(sampleEntry));
        PricelistEntryDto dto = service.getBySku("5021;6");
        assertThat(dto.sku()).isEqualTo("5021;6");
        assertThat(dto.buy()).isNotNull();
        assertThat(dto.buy().keys()).isEqualTo(1);
    }

    @Test
    void getBySku_notFound_throws() {
        when(repo.findById("999;6")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getBySku("999;6"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999;6");
    }

    @Test
    void upsert_createsNew() {
        when(repo.findById("100;6")).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PricelistEntryDto dto = new PricelistEntryDto(
                "100;6", true, false, 2, 0, 1,
                new CurrenciesDto(1, new BigDecimal("0.11")),
                new CurrenciesDto(1, new BigDecimal("0.33")),
                null, false, false, null, null
        );
        PricelistEntryDto result = service.upsert(dto);
        assertThat(result.sku()).isEqualTo("100;6");
        verify(repo).save(any(PricelistEntry.class));
    }

    @Test
    void delete_notFound_throws() {
        when(repo.existsById("999;6")).thenReturn(false);
        assertThatThrownBy(() -> service.delete("999;6"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_existing_callsRepo() {
        when(repo.existsById("5021;6")).thenReturn(true);
        service.delete("5021;6");
        verify(repo).deleteById("5021;6");
    }
}
