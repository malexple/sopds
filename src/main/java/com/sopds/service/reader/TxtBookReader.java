package com.sopds.service.reader;

import com.sopds.dto.BookContentDto;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TxtBookReader implements BookReader {

    private static final int CHARS_PER_PAGE = 5000; // символов на страницу

    @Override
    public String getFormat() {
        return "txt";
    }

    @Override
    public boolean supports(String format) {
        return "txt".equalsIgnoreCase(format);
    }

    @Override
    public BookContentDto read(InputStream inputStream, Long bookId, String title, int page) throws Exception {
        // Определяем кодировку (пробуем UTF-8, потом Windows-1251)
        String content = readWithEncoding(inputStream, StandardCharsets.UTF_8);

        // Разбиваем на страницы
        List<String> pages = splitIntoPages(content, CHARS_PER_PAGE);
        int totalPages = pages.size();
        page = Math.max(1, Math.min(page, totalPages));

        String pageContent = pages.isEmpty() ? "" : pages.get(page - 1);

        // Преобразуем в HTML
        String htmlContent = textToHtml(pageContent);

        return BookContentDto.builder()
                .bookId(bookId)
                .title(title)
                .format("txt")
                .content(htmlContent)
                .currentPage(page)
                .totalPages(totalPages)
                .chapters(List.of())
                .encoding("UTF-8")
                .supportsReading(true)
                .build();
    }

    private String readWithEncoding(InputStream inputStream, Charset charset) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private List<String> splitIntoPages(String content, int charsPerPage) {
        List<String> pages = new ArrayList<>();
        int length = content.length();

        for (int i = 0; i < length; i += charsPerPage) {
            int end = Math.min(i + charsPerPage, length);
            // Ищем конец абзаца для красивого разбиения
            if (end < length) {
                int lastNewline = content.lastIndexOf("\n", end);
                if (lastNewline > i) {
                    end = lastNewline + 1;
                }
            }
            pages.add(content.substring(i, end));
            i = end - charsPerPage; // корректируем для следующей итерации
        }

        if (pages.isEmpty()) {
            pages.add(content);
        }

        return pages;
    }

    private String textToHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n\n", "</p><p>")
                .replace("\n", "<br/>")
                .transform(s -> "<p>" + s + "</p>");
    }
}
