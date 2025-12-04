package com.sopds.controller;

import com.sopds.domain.Book;
import com.sopds.dto.BookContentDto;
import com.sopds.repository.BookRepository;
import com.sopds.service.BookReaderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ReaderController {

    private final BookReaderService bookReaderService;
    private final BookRepository bookRepository;

    private static final Set<String> READABLE_FORMATS = Set.of("txt", "fb2", "pdf", "djvu", "epub");

    @GetMapping("/read/{id}")
    @Transactional(readOnly = true)
    public String readBook(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            Model model) {

        log.info("Reading book id={}, page={}", id, page);

        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            model.addAttribute("error", "Книга не найдена");
            return "sopds_reader";
        }

        // Инициализируем lazy-коллекции
        if (book.getAuthors() != null) book.getAuthors().size();
        if (book.getGenres() != null) book.getGenres().size();
        if (book.getSeries() != null) book.getSeries().size();

        String format = book.getFormat() != null ? book.getFormat().toLowerCase() : "";

        // Для PDF и DJVU используем встроенный просмотрщик
        if ("pdf".equals(format) || "djvu".equals(format)) {
            model.addAttribute("book", book);
            model.addAttribute("embedMode", true);
            model.addAttribute("breadcrumbs", List.of("Читать", book.getTitle()));
            return "sopds_reader_embed";
        }

        BookContentDto bookContent = bookReaderService.readBook(id, page);

        model.addAttribute("book", book);
        model.addAttribute("bookContent", bookContent);  // Переименовали!
        model.addAttribute("breadcrumbs", List.of("Читать", book.getTitle()));
        model.addAttribute("current", "reader");

        return "sopds_reader";
    }

    /**
     * Проверка поддержки формата для чтения
     */
    public static boolean isReadable(String format) {
        return format != null && READABLE_FORMATS.contains(format.toLowerCase());
    }
}
