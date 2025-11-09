package com.sopds.catalog.service;

import com.sopds.catalog.config.ScannerConfiguration;
import com.sopds.catalog.entity.Author;
import com.sopds.catalog.entity.Book;
import com.sopds.catalog.entity.Genre;
import com.sopds.catalog.entity.Series;
import com.sopds.catalog.repository.AuthorRepository;
import com.sopds.catalog.repository.BookRepository;
import com.sopds.catalog.repository.GenreRepository;
import com.sopds.catalog.repository.SeriesRepository;
import com.sopds.catalog.service.parser.Fb2Parser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.hibernate.internal.util.StringHelper.truncate;


@Slf4j
@Service
@RequiredArgsConstructor
public class BookScannerService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final SeriesRepository seriesRepository;
    private final ScannerConfiguration config;
    private final Fb2Parser fb2Parser;

    private final AtomicInteger processedFiles = new AtomicInteger(0);
    private final AtomicInteger addedBooks = new AtomicInteger(0);
    private final AtomicInteger updatedBooks = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @Transactional
    public void scanLibrary() {
        log.info("Starting library scan from: {}", config.getRootPath());

        LocalDateTime startTime = LocalDateTime.now();
        resetCounters();

        Path libraryPath = Paths.get(config.getRootPath());

        if (!Files.exists(libraryPath)) {
            log.error("Library path does not exist: {}", libraryPath);
            try {
                Files.createDirectories(libraryPath);
                log.info("Created library directory: {}", libraryPath);
            } catch (IOException e) {
                log.error("Failed to create library directory", e);
                return;
            }
        }

        try {
            Files.walkFileTree(libraryPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    processFile(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    log.warn("Failed to access file: {}", file, exc);
                    errorCount.incrementAndGet();
                    return FileVisitResult.CONTINUE;
                }
            });

            LocalDateTime endTime = LocalDateTime.now();
            log.info("Library scan completed in {} seconds",
                    java.time.Duration.between(startTime, endTime).getSeconds());
            log.info("Statistics - Processed: {}, Added: {}, Updated: {}, Errors: {}",
                    processedFiles.get(), addedBooks.get(), updatedBooks.get(), errorCount.get());

        } catch (IOException e) {
            log.error("Error during library scan", e);
        }
    }

    private void processFile(Path filePath) {
        String filename = filePath.getFileName().toString().toLowerCase();
        String extension = getFileExtension(filename);

        // Check if file size is within limits
        try {
            long fileSizeBytes = Files.size(filePath);
            long fileSizeMb = fileSizeBytes / (1024 * 1024);

            if (fileSizeMb > config.getMaxFileSizeMb()) {
                log.debug("Skipping file (too large): {} ({} MB)", filePath, fileSizeMb);
                return;
            }
        } catch (IOException e) {
            log.warn("Failed to get file size: {}", filePath);
        }

        if (config.isZipScanEnabled() && "zip".equals(extension)) {
            processZipFile(filePath);
        } else if (config.getSupportedFormats().contains(extension)) {
            processSingleFile(filePath);
        }
    }

    @Transactional
    public void processSingleFile(Path filePath) {
        try {
            processedFiles.incrementAndGet();

            String relativePath = Paths.get(config.getRootPath()).relativize(filePath).toString();
            String extension = getFileExtension(filePath.getFileName().toString());

            // Check if book already exists
            Optional<Book> existingBook = bookRepository.findByPath(relativePath);
            if (existingBook.isPresent()) {
                log.debug("Book already exists: {}", relativePath);
                return;
            }

            // Parse metadata based on format
            if ("fb2".equals(extension)) {
                processFb2File(filePath, relativePath);
            } else {
                // For other formats, create basic book entry
                processGenericFile(filePath, relativePath, extension);
            }

        } catch (Exception e) {
            log.error("Error processing file: {}", filePath, e);
            errorCount.incrementAndGet();
        }
    }

    private void processFb2File(Path filePath, String relativePath) {
        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            Fb2Parser.Fb2Metadata metadata = fb2Parser.parse(fis);

            if (metadata == null || metadata.getTitle() == null) {
                log.warn("Failed to parse FB2 metadata: {}", filePath);
                processGenericFile(filePath, relativePath, "fb2");
                return;
            }

            Book book = createBookFromMetadata(metadata, filePath, relativePath, "fb2");
            bookRepository.save(book);
            addedBooks.incrementAndGet();

            log.debug("Added book: {} by {}", book.getTitle(),
                    book.getAuthors().isEmpty() ? "Unknown" :
                            book.getAuthors().iterator().next().getFullName());

        } catch (Exception e) {
            log.error("Error processing FB2 file: {}", filePath, e);
            errorCount.incrementAndGet();
        }
    }

    private void processGenericFile(Path filePath, String relativePath, String format) {
        try {
            String filename = filePath.getFileName().toString();
            String title = filename.substring(0, filename.lastIndexOf('.'));

            Book book = Book.builder()
                    .title(title)
                    .titleSort(title)
                    .path(relativePath)
                    .filename(filename)
                    .format(format.toUpperCase())
                    .filesize(BigDecimal.valueOf(Files.size(filePath) / (1024.0 * 1024.0)))
                    .available(true)
                    .build();

            bookRepository.save(book);
            addedBooks.incrementAndGet();

            log.debug("Added generic book: {}", title);

        } catch (Exception e) {
            log.error("Error processing generic file: {}", filePath, e);
            errorCount.incrementAndGet();
        }
    }

    private void processZipFile(Path zipPath) {
        try (ZipFile zipFile = new ZipFile(zipPath.toFile(),
                java.nio.charset.Charset.forName(config.getZipEncoding()))) {

            zipFile.stream()
                    .filter(entry -> !entry.isDirectory())
                    .filter(entry -> {
                        String ext = getFileExtension(entry.getName());
                        return config.getSupportedFormats().contains(ext);
                    })
                    .forEach(entry -> processZipEntry(zipFile, entry, zipPath));

        } catch (IOException e) {
            log.error("Error processing ZIP file: {}", zipPath, e);
            errorCount.incrementAndGet();
        }
    }

    private void processZipEntry(ZipFile zipFile, ZipEntry entry, Path zipPath) {
        try {
            processedFiles.incrementAndGet();

            String relativePath = Paths.get(config.getRootPath()).relativize(zipPath).toString();
            String entryPath = relativePath + "/" + entry.getName();
            String extension = getFileExtension(entry.getName());

            // Check if book already exists
            if (bookRepository.existsByPath(entryPath)) {
                log.debug("Book already exists in ZIP: {}", entryPath);
                return;
            }

            // Parse FB2 files from ZIP
            if ("fb2".equals(extension)) {
                Fb2Parser.Fb2Metadata metadata = fb2Parser.parseFromZip(zipFile, entry);

                if (metadata != null && metadata.getTitle() != null) {
                    Book book = createBookFromMetadata(metadata, null, entryPath, "fb2");
                    book.setFilename(entry.getName());
                    book.setFilesize(BigDecimal.valueOf(entry.getSize() / (1024.0 * 1024.0)));

                    bookRepository.save(book);
                    addedBooks.incrementAndGet();

                    log.debug("Added book from ZIP: {}", book.getTitle());
                    return;
                }
            }

            // Generic entry for other formats
            String filename = new File(entry.getName()).getName();
            String title = filename.substring(0, filename.lastIndexOf('.'));

            Book book = Book.builder()
                    .title(title)
                    .titleSort(title)
                    .path(entryPath)
                    .filename(filename)
                    .format(extension.toUpperCase())
                    .filesize(BigDecimal.valueOf(entry.getSize() / (1024.0 * 1024.0)))
                    .available(true)
                    .build();

            bookRepository.save(book);
            addedBooks.incrementAndGet();

        } catch (Exception e) {
            log.error("Error processing ZIP entry: {}", entry.getName(), e);
            errorCount.incrementAndGet();
        }
    }

    private Book createBookFromMetadata(Fb2Parser.Fb2Metadata metadata, Path filePath,
                                        String relativePath, String format) throws IOException {
        Book book = Book.builder()
                .title(truncate(metadata.getTitle(), 500))
                .titleSort(truncate(metadata.getTitle(), 500))
                .annotation(metadata.getAnnotation()) // TEXT field, no truncation needed
                .lang(metadata.getLang() != null ? truncate(metadata.getLang(), 10) : null)
                .isbn(metadata.getIsbn() != null ? truncate(metadata.getIsbn(), 50) : null)
                .path(truncate(relativePath, 1000))
                .format(format.toUpperCase())
                .available(true)
                .build();

        if (filePath != null) {
            book.setFilename(filePath.getFileName().toString());
            book.setFilesize(BigDecimal.valueOf(Files.size(filePath) / (1024.0 * 1024.0)));
        }

        // Parse and set publish date
        if (metadata.getDate() != null) {
            book.setPublishDate(parseDate(metadata.getDate()));
        }

        // Add authors
        if (metadata.getAuthors() != null) {
            for (Fb2Parser.Fb2Metadata.AuthorInfo authorInfo : metadata.getAuthors()) {
                Author author = findOrCreateAuthor(authorInfo);
                book.addAuthor(author);
            }
        }

        // Add genres
        if (metadata.getGenres() != null) {
            for (String genreCode : metadata.getGenres()) {
                genreRepository.findByCode(genreCode).ifPresent(book::addGenre);
            }
        }

        // Add series
        if (metadata.getSeriesName() != null && !metadata.getSeriesName().isEmpty()) {
            Series series = findOrCreateSeries(metadata.getSeriesName());
            book.setSeries(series);
            book.setSeriesNumber(metadata.getSeriesNumber());
        }

        return book;
    }

    private Author findOrCreateAuthor(Fb2Parser.Fb2Metadata.AuthorInfo authorInfo) {
        String fullName = authorInfo.getFullName();

        return authorRepository.findByFullName(fullName)
                .orElseGet(() -> {
                    Author author = Author.builder()
                            .fullName(fullName)
                            .firstName(authorInfo.getFirstName())
                            .middleName(authorInfo.getMiddleName())
                            .lastName(authorInfo.getLastName())
                            .fullNameSort(fullName)
                            .build();
                    return authorRepository.save(author);
                });
    }

    private Series findOrCreateSeries(String seriesName) {
        return seriesRepository.findByName(seriesName)
                .orElseGet(() -> {
                    Series series = Series.builder()
                            .name(seriesName)
                            .nameSort(seriesName)
                            .build();
                    return seriesRepository.save(series);
                });
    }

    private LocalDate parseDate(String dateStr) {
        try {
            // Try different date formats
            String[] formats = {"yyyy-MM-dd", "yyyy", "dd.MM.yyyy", "dd/MM/yyyy"};
            for (String format : formats) {
                try {
                    if (format.equals("yyyy") && dateStr.length() == 4) {
                        return LocalDate.of(Integer.parseInt(dateStr), 1, 1);
                    }
                    return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(format));
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse date: {}", dateStr);
        }
        return null;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    private void resetCounters() {
        processedFiles.set(0);
        addedBooks.set(0);
        updatedBooks.set(0);
        errorCount.set(0);
    }

    public void logStatistics() {
        log.info("Current scan statistics - Processed: {}, Added: {}, Updated: {}, Errors: {}",
                processedFiles.get(), addedBooks.get(), updatedBooks.get(), errorCount.get());
    }
}