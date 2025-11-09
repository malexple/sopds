package com.sopds.catalog.controller;

import com.sopds.catalog.service.BookScannerService;
import com.sopds.catalog.service.ScannerSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Scanner API", description = "API для управления сканированием библиотеки")
public class ScannerController {

    private final ScannerSchedulerService schedulerService;
    private final BookScannerService scannerService;

    /**
     * Trigger manual library scan
     */
    @PostMapping("/scan")
    @Operation(
            summary = "Запустить сканирование библиотеки",
            description = "Запускает ручное сканирование библиотеки книг"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Сканирование успешно запущено",
                    content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Сканирование уже выполняется",
                    content = @Content(schema = @Schema(implementation = Map.class))
            )
    })
    public ResponseEntity<Map<String, Object>> triggerScan() {
        log.info("Manual scan triggered via API");

        if (schedulerService.isCurrentlyScanning()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Scan already in progress");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }

        // Run scan in separate thread to avoid blocking
        new Thread(() -> schedulerService.triggerManualScan()).start();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Library scan started");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Check scan status
     */
    @GetMapping("/status")
    @Operation(
            summary = "Получить статус сканирования",
            description = "Возвращает текущий статус процесса сканирования"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Статус получен успешно",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    public ResponseEntity<Map<String, Object>> getScanStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("isScanning", schedulerService.isCurrentlyScanning());
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get scan statistics
     */
    @GetMapping("/stats")
    @Operation(
            summary = "Получить статистику сканирования",
            description = "Возвращает статистику по последнему сканированию"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Статистика получена успешно",
            content = @Content(schema = @Schema(implementation = Map.class))
    )
    public ResponseEntity<Map<String, Object>> getStats() {
        scannerService.logStatistics();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Check logs for statistics");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}