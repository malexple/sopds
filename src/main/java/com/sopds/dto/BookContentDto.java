package com.sopds.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookContentDto {

    private Long bookId;
    private String title;
    private String format;
    private String content;        // HTML-контент для отображения
    private int currentPage;
    private int totalPages;
    private List<String> chapters; // Список глав (для навигации)
    private String encoding;
    private boolean supportsReading; // Поддерживается ли чтение данного формата
}
