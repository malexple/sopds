package com.sopds.service;

import com.sopds.domain.ScanHistory;
import com.sopds.repository.ScanHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ScanHistoryService {

    private final ScanHistoryRepository scanHistoryRepository;

    @Transactional(readOnly = true)
    public Optional<ScanHistory> getLastSuccessful() {
        return scanHistoryRepository.findLastSuccessfulScan();
    }

    @Transactional(readOnly = true)
    public Page<ScanHistory> getAll(int page, int size) {
        return scanHistoryRepository.findAllByOrderByScanDateDesc(PageRequest.of(page, size));
    }

    public ScanHistory startScan() {
        log.info("Starting scan");

        ScanHistory history = ScanHistory.builder()
                .scanDate(LocalDateTime.now())
                .status("IN_PROGRESS")
                .build();

        return scanHistoryRepository.save(history);
    }

    public ScanHistory completeScan(Long id, int added, int updated, int deleted, long durationMs) {
        log.info("Completing scan ID: {}, added: {}, updated: {}, deleted: {}", id, added, updated, deleted);

        ScanHistory history = scanHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scan not found: " + id));

        history.setBooksAdded(added);
        history.setBooksUpdated(updated);
        history.setBooksDeleted(deleted);
        history.setDurationMs(durationMs);
        history.setStatus("SUCCESS");

        return scanHistoryRepository.save(history);
    }

    public ScanHistory failScan(Long id, String errorMessage) {
        log.error("Scan failed ID: {}, error: {}", id, errorMessage);

        ScanHistory history = scanHistoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Scan not found: " + id));

        history.setStatus("FAILED");
        history.setErrorMessage(errorMessage);

        return scanHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    public long countSuccessful() {
        return scanHistoryRepository.countByStatus("SUCCESS");
    }
}
