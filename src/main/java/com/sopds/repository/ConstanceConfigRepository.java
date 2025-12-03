package com.sopds.repository;

import com.sopds.domain.ConstanceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConstanceConfigRepository extends JpaRepository<ConstanceConfig, Long> {

    Optional<ConstanceConfig> findByKey(String key);

    boolean existsByKey(String key);

    void deleteByKey(String key);
}
