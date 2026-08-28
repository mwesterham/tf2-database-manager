package com.tf2autobot.dbmanager.service;

import com.tf2autobot.dbmanager.dto.PollDataDto;
import com.tf2autobot.dbmanager.entity.PollDataEntry;
import com.tf2autobot.dbmanager.entity.Setting;
import com.tf2autobot.dbmanager.exception.NotFoundException;
import com.tf2autobot.dbmanager.repository.PollDataRepository;
import com.tf2autobot.dbmanager.repository.SettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PollDataService {

    private static final String OFFER_SINCE_KEY = "offer_since";

    private final PollDataRepository pollRepo;
    private final SettingRepository settingRepo;

    public PollDataService(PollDataRepository pollRepo, SettingRepository settingRepo) {
        this.pollRepo = pollRepo;
        this.settingRepo = settingRepo;
    }

    @Transactional(readOnly = true)
    public PollDataDto getAll() {
        List<PollDataDto.PollDataEntryDto> entries = pollRepo.findAll().stream()
                .map(PollDataDto.PollDataEntryDto::from).toList();
        return new PollDataDto(entries);
    }

    @Transactional(readOnly = true)
    public String getOfferSince() {
        return settingRepo.findById(OFFER_SINCE_KEY)
                .map(Setting::getValue)
                .orElse("0");
    }

    public void upsertEntry(String offerId, String direction, int state, String partnerSteamId64) {
        PollDataEntry entry = pollRepo.findById(offerId).orElseGet(() -> {
            PollDataEntry e = new PollDataEntry();
            e.setOfferId(offerId);
            return e;
        });
        entry.setDirection(direction);
        entry.setState(state);
        entry.setPartnerSteamId64(partnerSteamId64);
        pollRepo.save(entry);
    }

    public void setOfferSince(String timestamp) {
        Setting setting = settingRepo.findById(OFFER_SINCE_KEY)
                .orElseGet(() -> new Setting(OFFER_SINCE_KEY, "0"));
        setting.setValue(timestamp);
        settingRepo.save(setting);
    }

    public void deleteEntry(String offerId) {
        if (!pollRepo.existsById(offerId)) throw new NotFoundException("Offer not found: " + offerId);
        pollRepo.deleteById(offerId);
    }

    public void clearAll() {
        pollRepo.deleteAll();
        setOfferSince("0");
    }

    @Transactional(readOnly = true)
    public Map<String, String> getSettings() {
        return settingRepo.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Setting::getKey, Setting::getValue));
    }
}
