package com.sopds.repository;

import com.sopds.domain.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingsRepository extends JpaRepository<AppSettings, Long> {

    Optional<AppSettings> findByKey(String key);

    boolean existsByKey(String key);

    void deleteByKey(String key);
}
