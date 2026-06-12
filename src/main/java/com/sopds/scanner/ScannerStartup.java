package com.sopds.scanner;

import com.sopds.config.SopdsProperties;
import com.sopds.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScannerStartup {

    private final SopdsProperties properties;
    private final ConfigService configService;
    private final LibraryScanner libraryScanner;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (properties.isScanOnStartup()) {
            log.info("Scan on startup is enabled. Starting library scan...");

            Thread scanThread = new Thread(() -> {
                try {
                    String rootPath = configService.getString("SOPDS_ROOT_LIB", properties.getRootLib());
                    ScanResult result = libraryScanner.scan(rootPath);

                    if (result.isSuccess()) {
                        log.info("Startup scan completed. Added: {}, Skipped: {}",
                                result.getBooksAdded(), result.getBooksSkipped());
                    } else {
                        log.error("Startup scan failed: {}", result.getError());
                    }
                } catch (Exception e) {
                    log.error("Startup scan error", e);
                }
            }, "library-scanner");

            scanThread.setDaemon(true);
            scanThread.start();
        } else {
            log.info("Scan on startup is disabled");
        }
    }
}