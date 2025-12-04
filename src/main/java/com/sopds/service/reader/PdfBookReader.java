package com.sopds.service.reader;

import com.sopds.dto.BookContentDto;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class PdfBookReader implements BookReader {

    @Override
    public String getFormat() {
        return "pdf";
    }

    @Override
    public boolean supports(String format) {
        return "pdf".equalsIgnoreCase(format);
    }

    @Override
    public BookContentDto read(InputStream inputStream, Long bookId, String title, int page) throws Exception {
        // PDF будет отображаться через встроенный просмотрщик браузера или PDF.js
        // Возвращаем специальный маркер
        return BookContentDto.builder()
                .bookId(bookId)
                .title(title)
                .format("pdf")
                .content("PDF_EMBED") // Специальный маркер для шаблона
                .currentPage(1)
                .totalPages(1)
                .chapters(List.of())
                .encoding("binary")
                .supportsReading(true)
                .build();
    }
}
