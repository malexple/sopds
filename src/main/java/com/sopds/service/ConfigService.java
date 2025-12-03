package com.sopds.service;

import com.sopds.domain.AppSettings;
import com.sopds.repository.AppSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConfigService {

    private final AppSettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public String getString(String key, String defaultValue) {
        return settingsRepository.findByKey(key)
                .map(AppSettings::getStringValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public Integer getInt(String key, Integer defaultValue) {
        return settingsRepository.findByKey(key)
                .map(AppSettings::getIntValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return settingsRepository.findByKey(key)
                .map(AppSettings::getBooleanValue)
                .orElse(defaultValue);
    }

    public void setString(String key, String value) {
        log.info("Setting config: {}={}", key, value);
        saveOrUpdate(key, value, "STRING");
    }

    public void setInt(String key, Integer value) {
        log.info("Setting config: {}={}", key, value);
        saveOrUpdate(key, value != null ? value.toString() : null, "INT");
    }

    public void setBoolean(String key, Boolean value) {
        log.info("Setting config: {}={}", key, value);
        saveOrUpdate(key, value != null ? value.toString() : null, "BOOLEAN");
    }

    private void saveOrUpdate(String key, String value, String type) {
        AppSettings settings = settingsRepository.findByKey(key)
                .orElse(AppSettings.builder().key(key).build());

        settings.setValue(value);
        settings.setType(type);
        settingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public List<AppSettings> getAll() {
        return settingsRepository.findAll();
    }

    public void delete(String key) {
        log.info("Deleting config: {}", key);
        settingsRepository.deleteByKey(key);
    }
}
