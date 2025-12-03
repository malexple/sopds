package com.sopds.scanner;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanResult {

    private boolean success;
    private int booksAdded;
    private int booksUpdated;
    private int booksSkipped;
    private int errors;
    private long durationMs;
    private String error;
}
