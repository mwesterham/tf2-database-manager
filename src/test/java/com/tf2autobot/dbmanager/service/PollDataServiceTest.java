package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.PollDataDto;
import com.tf2autobot.dbmanager.entity.PollDataEntry;
import com.tf2autobot.dbmanager.entity.Setting;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.PollDataRepository;
import com.tf2autobot.dbmanager.repository.SettingRepository;
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
class PollDataServiceTest {

    @Mock
    private PollDataRepository pollRepo;

    @Mock
    private SettingRepository settingRepo;

    @InjectMocks
    private PollDataService service;

    @Test
    void getAll_returnsMappedEntries() {
        PollDataEntry e = new PollDataEntry();
        e.setOfferId("offer1");
        e.setDirection("received");
        e.setState(2);
        when(pollRepo.findAll()).thenReturn(List.of(e));

        PollDataDto result = service.getAll();
        assertThat(result.entries()).hasSize(1);
        assertThat(result.entries().get(0).offerId()).isEqualTo("offer1");
    }

    @Test
    void getOfferSince_returnsSavedValue() {
        Setting s = new Setting("offer_since", "1700000000");
        when(settingRepo.findById("offer_since")).thenReturn(Optional.of(s));
        assertThat(service.getOfferSince()).isEqualTo("1700000000");
    }

    @Test
    void getOfferSince_defaultsToZero() {
        when(settingRepo.findById("offer_since")).thenReturn(Optional.empty());
        assertThat(service.getOfferSince()).isEqualTo("0");
    }

    @Test
    void upsertEntry_createsNew() {
        when(pollRepo.findById("offer1")).thenReturn(Optional.empty());
        when(pollRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.upsertEntry("offer1", "received", 2, "76561198000000001");
        verify(pollRepo).save(any(PollDataEntry.class));
    }

    @Test
    void deleteEntry_notFound_throws() {
        when(pollRepo.existsById("offer1")).thenReturn(false);
        assertThatThrownBy(() -> service.deleteEntry("offer1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setOfferSince_savesNewValue() {
        when(settingRepo.findById("offer_since")).thenReturn(Optional.empty());
        when(settingRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service.setOfferSince("1700001234");
        verify(settingRepo).save(argThat(s -> "1700001234".equals(s.getValue())));
    }
}
