package com.sopds.catalog.controller;

import com.sopds.catalog.service.BookScannerService;
import com.sopds.catalog.service.ScannerSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
public class ScannerController {

    private final ScannerSchedulerService schedulerService;
    private final BookScannerService scannerService;

    /**
     * Trigger manual library scan
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> triggerScan() {
        log.info("Manual scan triggered via API");

        if (schedulerService.isCurrentlyScanning()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Scan already in progress");
            return ResponseEntity.badRequest().body(response);
        }

        // Run scan in separate thread to avoid blocking
        new Thread(() -> schedulerService.triggerManualScan()).start();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Library scan started");
        return ResponseEntity.ok(response);
    }

    /**
     * Check scan status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("isScanning", schedulerService.isCurrentlyScanning());
        return ResponseEntity.ok(response);
    }

    /**
     * Get scan statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        scannerService.logStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Check logs for statistics");
        return ResponseEntity.ok(response);
    }
}