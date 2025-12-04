package com.sopds.controller;

import com.sopds.domain.Book;
import com.sopds.repository.BookRepository;
import com.sopds.service.BookFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/opds")
@RequiredArgsConstructor
@Slf4j
public class OpdsController {

    private final BookRepository bookRepository;
    private final BookFileService bookFileService;

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            Map.entry("fb2", "application/x-fictionbook+xml"),
            Map.entry("epub", "application/epub+zip"),
            Map.entry("mobi", "application/x-mobipocket-ebook"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("djvu", "image/vnd.djvu"),
            Map.entry("djv", "image/vnd.djvu"),
            Map.entry("txt", "text/plain; charset=utf-8"),
            Map.entry("rtf", "application/rtf"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("zip", "application/zip")
    );

    /**
     * Скачивание книги
     */
    @GetMapping("/download/{id}/{zip}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadBook(
            @PathVariable Long id,
            @PathVariable int zip) {

        log.info("Download request: bookId={}, zip={}", id, zip);

        Book book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            log.warn("Book not found: {}", id);
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] content = bookFileService.getBookBytes(book);
            String filename = book.getFilename();
            String format = book.getFormat() != null ? book.getFormat().toLowerCase() : "bin";

            if (zip == 1) {
                content = wrapInZip(content, filename);
                filename = filename + ".zip";
                format = "zip";
            }

            String mimeType = MIME_TYPES.getOrDefault(format, "application/octet-stream");
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replace("+", "%20");

            log.info("Sending file: {}, size: {} bytes", filename, content.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                    .header(HttpHeaders.CONTENT_TYPE, mimeType)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                    .body(new InputStreamResource(new ByteArrayInputStream(content)));

        } catch (Exception e) {
            log.error("Error downloading book {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Заглушка для обложки
     */
    @GetMapping("/cover/{id}")
    public ResponseEntity<Resource> getBookCover(@PathVariable Long id) {
        return ResponseEntity.notFound().build();
    }

    private byte[] wrapInZip(byte[] content, String filename) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(filename);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
