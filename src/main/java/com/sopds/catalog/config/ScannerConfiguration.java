package com.sopds.catalog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "sopds.library")
@Getter
@Setter
public class ScannerConfiguration {

    /**
     * Root path to the book library
     */
    private String rootPath = "books";

    /**
     * Enable scanning at startup
     */
    private boolean scanOnStartup = true;

    /**
     * Scan interval in milliseconds (default: 12 hours)
     */
    private long scanInterval = 43200000L;

    /**
     * Cron expression for scheduled scanning (overrides scanInterval if set)
     * Example: "0 0 3 10 * ?" - At 03:00 AM, on the 10th day, every month
     */
    private String scanCron = "0 0 3 * * ?"; // Daily at 3 AM

    /**
     * Supported book formats
     */
    private Set<String> supportedFormats = Set.of("fb2", "epub", "pdf", "djvu", "mobi");

    /**
     * Enable ZIP archive scanning
     */
    private boolean zipScanEnabled = true;

    /**
     * ZIP file encoding for filenames
     */
    private String zipEncoding = "cp866";

    /**
     * Enable INPX file processing
     */
    private boolean inpxEnabled = true;

    /**
     * Skip INPX files if size unchanged
     */
    private boolean inpxSkipUnchanged = true;

    /**
     * Test if ZIP files exist when processing INPX
     */
    private boolean inpxTestZip = false;

    /**
     * Test if individual files exist in ZIP when processing INPX
     */
    private boolean inpxTestFiles = false;

    /**
     * Number of threads for parallel scanning
     */
    private int scanThreads = 4;

    /**
     * Maximum file size to process in MB
     */
    private long maxFileSizeMb = 100;
}