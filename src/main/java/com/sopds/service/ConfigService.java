package com.sopds.service;

import com.sopds.domain.ConstanceConfig;
import com.sopds.repository.ConstanceConfigRepository;
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

    private final ConstanceConfigRepository configRepository;

    @Transactional(readOnly = true)
    public String getString(String key, String defaultValue) {
        return configRepository.findByKey(key)
                .map(ConstanceConfig::getValue)
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public Integer getInt(String key, Integer defaultValue) {
        return configRepository.findByKey(key)
                .map(c -> Integer.parseInt(c.getValue()))
                .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public Boolean getBoolean(String key, Boolean defaultValue) {
        return configRepository.findByKey(key)
                .map(c -> Boolean.parseBoolean(c.getValue()))
                .orElse(defaultValue);
    }

    public void setString(String key, String value) {
        log.info("Setting config: {}={}", key, value);

        ConstanceConfig config = configRepository.findByKey(key)
                .orElse(ConstanceConfig.builder().key(key).build());

        config.setValue(value);
        configRepository.save(config);
    }

    @Transactional(readOnly = true)
    public List<ConstanceConfig> getAll() {
        return configRepository.findAll();
    }

    public void delete(String key) {
        log.info("Deleting config: {}", key);
        configRepository.deleteByKey(key);
    }
}
