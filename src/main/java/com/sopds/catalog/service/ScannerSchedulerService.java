package com.sopds.catalog.service;

import com.sopds.catalog.config.ScannerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerSchedulerService {

    private final BookScannerService scannerService;
    private final ScannerConfiguration config;

    private final AtomicBoolean isScanning = new AtomicBoolean(false);

    /**
     * Scan library on application startup if configured
     */
    @EventListener(ApplicationReadyEvent.class)
    public void scanOnStartup() {
        if (config.isScanOnStartup()) {
            log.info("Starting initial library scan on application startup...");
            executeScan();
        } else {
            log.info("Startup scanning is disabled");
        }
    }

    /**
     * Scheduled library scan based on cron expression from configuration
     * Default: Daily at 3 AM (0 0 3 * * ?)
     */
    @Scheduled(cron = "${sopds.library.scan-cron:0 0 3 * * ?}")
    public void scheduledScan() {
        log.info("Starting scheduled library scan...");
        executeScan();
    }

    /**
     * Manual trigger for library scan
     */
    public void triggerManualScan() {
        log.info("Starting manual library scan...");
        executeScan();
    }

    private void executeScan() {
        if (!isScanning.compareAndSet(false, true)) {
            log.warn("Scan already in progress, skipping...");
            return;
        }

        try {
            log.info("=== Library Scan Started ===");
            scannerService.scanLibrary();
            log.info("=== Library Scan Completed ===");
        } catch (Exception e) {
            log.error("Error during library scan", e);
        } finally {
            isScanning.set(false);
        }
    }

    public boolean isCurrentlyScanning() {
        return isScanning.get();
    }
}