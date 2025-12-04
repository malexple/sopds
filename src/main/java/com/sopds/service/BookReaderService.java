package com.sopds.service;

import com.sopds.domain.Book;
import com.sopds.dto.BookContentDto;
import com.sopds.service.reader.BookReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookReaderService {

    private final List<BookReader> readers;
    private final BookService bookService;
    private final BookFileService bookFileService;

    private static final Set<String> SUPPORTED_FORMATS = Set.of("txt", "fb2", "pdf", "djvu", "epub");

    public boolean isReadingSupported(String format) {
        return SUPPORTED_FORMATS.contains(format.toLowerCase());
    }

    public BookContentDto readBook(Long bookId, int page) {
        Book book = bookService.getById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        String format = book.getFormat().toLowerCase();

        // Находим подходящий reader
        BookReader reader = readers.stream()
                .filter(r -> r.supports(format))
                .findFirst()
                .orElse(null);

        if (reader == null) {
            return BookContentDto.builder()
                    .bookId(bookId)
                    .title(book.getTitle())
                    .format(format)
                    .content("<p>Формат " + format.toUpperCase() + " не поддерживается для чтения в браузере.</p>")
                    .currentPage(1)
                    .totalPages(1)
                    .supportsReading(false)
                    .build();
        }

        try {
            InputStream inputStream = bookFileService.getBookInputStream(book);
            return reader.read(inputStream, bookId, book.getTitle(), page);
        } catch (Exception e) {
            log.error("Error reading book {}: {}", bookId, e.getMessage(), e);
            return BookContentDto.builder()
                    .bookId(bookId)
                    .title(book.getTitle())
                    .format(format)
                    .content("<p>Ошибка при чтении книги: " + e.getMessage() + "</p>")
                    .currentPage(1)
                    .totalPages(1)
                    .supportsReading(false)
                    .build();
        }
    }
}
