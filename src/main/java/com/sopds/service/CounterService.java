package com.sopds.service;

import com.sopds.domain.Counter;
import com.sopds.repository.CounterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;

    public int get(String name) {
        return counterRepository.findByName(name)
                .map(Counter::getValue)
                .orElse(0);
    }

    @Transactional
    public void set(String name, int value) {
        Counter counter = counterRepository.findByName(name)
                .orElseGet(() -> Counter.builder().name(name).build());
        counter.setValue(value);
        counterRepository.save(counter);
    }

    @Transactional
    public void increment(String name) {
        Counter counter = counterRepository.findByName(name)
                .orElseGet(() -> Counter.builder().name(name).value(0).build());
        counter.setValue(counter.getValue() + 1);
        counterRepository.save(counter);
    }

    public LocalDateTime getLastScanDate() {
        return counterRepository.findByName("lastscan")
                .map(c -> {
                    try {
                        return LocalDateTime.parse(String.valueOf(c.getValue()),
                                DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                    } catch (Exception e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    @Transactional
    public void setLastScanDate(LocalDateTime dateTime) {
        String value = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Counter counter = counterRepository.findByName("lastscan")
                .orElseGet(() -> Counter.builder().name("lastscan").build());
        counter.setValue(Integer.parseInt(value.substring(0, 8))); // Сохраняем только дату
        counterRepository.save(counter);
    }
}
