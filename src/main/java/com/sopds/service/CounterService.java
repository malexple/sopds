package com.sopds.service;

import com.sopds.domain.Counter;
import com.sopds.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CounterService {

    private final CounterRepository counterRepository;

    @Transactional(readOnly = true)
    public Optional<Counter> get(String name) {
        return counterRepository.findById(name);
    }

    @Transactional(readOnly = true)
    public Integer getValue(String name) {
        return counterRepository.findById(name)
                .map(Counter::getValue)
                .orElse(0);
    }

    public Counter set(String name, Integer value) {
        log.debug("Setting counter {}: {}", name, value);

        Counter counter = counterRepository.findById(name)
                .orElse(Counter.builder().name(name).build());

        counter.setValue(value);
        counter.setUpdateTime(LocalDateTime.now());

        return counterRepository.save(counter);
    }

    public Counter increment(String name) {
        Counter counter = counterRepository.findById(name)
                .orElse(Counter.builder().name(name).value(0).build());

        counter.setValue(counter.getValue() + 1);
        counter.setUpdateTime(LocalDateTime.now());

        return counterRepository.save(counter);
    }

    public Counter decrement(String name) {
        Counter counter = counterRepository.findById(name)
                .orElse(Counter.builder().name(name).value(0).build());

        counter.setValue(Math.max(0, counter.getValue() - 1));
        counter.setUpdateTime(LocalDateTime.now());

        return counterRepository.save(counter);
    }
}
