package com.sopds.api;

import com.sopds.config.SopdsProperties;
import com.sopds.scanner.LibraryScanner;
import com.sopds.scanner.ScanResult;
import com.sopds.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/scanner")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Scanner", description = "API для сканирования библиотеки книг")
public class ScannerApiController {

    private final ConfigService configService;
    private final LibraryScanner libraryScanner;
    private final SopdsProperties properties;

    private final AtomicBoolean scanning = new AtomicBoolean(false);
    private volatile ScanResult lastResult = null;

    @PostMapping("/scan")
    @Operation(
            summary = "Запустить сканирование",
            description = "Запускает асинхронное сканирование библиотеки. Повторный вызов во время сканирования вернёт ошибку."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Сканирование запущено"),
            @ApiResponse(responseCode = "400", description = "Сканирование уже выполняется")
    })
    public ResponseEntity<Map<String, Object>> startScan() {
        if (scanning.get()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Scan already in progress",
                    "timestamp", System.currentTimeMillis()
            ));
        }

        // Запускаем в отдельном потоке
        Thread scanThread = new Thread(() -> {
            scanning.set(true);
            try {
                String rootPath = configService.getString("SOPDS_ROOT_LIB", properties.getRootLib());
                lastResult = libraryScanner.scan(rootPath);
            } catch (Exception e) {
                log.error("Scan failed", e);
                lastResult = ScanResult.builder()
                        .success(false)
                        .error(e.getMessage())
                        .build();
            } finally {
                scanning.set(false);
            }
        }, "api-scanner");
        scanThread.setDaemon(true);
        scanThread.start();

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Library scan started",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/status")
    @Operation(
            summary = "Статус сканирования",
            description = "Возвращает текущий статус процесса сканирования"
    )
    public Map<String, Object> getStatus() {
        return Map.of(
                "isScanning", scanning.get(),
                "timestamp", System.currentTimeMillis()
        );
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Статистика последнего сканирования",
            description = "Возвращает результаты последнего завершённого сканирования"
    )
    public Map<String, Object> getStats() {
        if (lastResult == null) {
            return Map.of(
                    "status", "no_data",
                    "message", "No scan has been performed yet",
                    "timestamp", System.currentTimeMillis()
            );
        }

        return Map.of(
                "status", lastResult.isSuccess() ? "success" : "error",
                "booksAdded", lastResult.getBooksAdded(),
                "booksUpdated", lastResult.getBooksUpdated(),
                "booksSkipped", lastResult.getBooksSkipped(),
                "errors", lastResult.getErrors(),
                "durationMs", lastResult.getDurationMs(),
                "timestamp", System.currentTimeMillis()
        );
    }
}
