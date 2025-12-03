package com.sopds.scanner;

import com.sopds.config.SopdsProperties;
import com.sopds.domain.*;
import com.sopds.repository.*;
import com.sopds.scanner.model.AuthorInfo;
import com.sopds.scanner.model.BookInfo;
import com.sopds.scanner.model.GenreInfo;
import com.sopds.scanner.model.SeriesInfo;
import com.sopds.scanner.parser.BookParser;
import com.sopds.service.CounterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryScanner {

    private final SopdsProperties properties;
    private final List<BookParser> parsers;
    private final CatalogRepository catalogRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final SeriesRepository seriesRepository;
    private final CounterService counterService;

    private final AtomicInteger booksAdded = new AtomicInteger(0);
    private final AtomicInteger booksUpdated = new AtomicInteger(0);
    private final AtomicInteger booksSkipped = new AtomicInteger(0);
    private final AtomicInteger errors = new AtomicInteger(0);

    @Transactional
    public ScanResult scan() {
        String rootPath = properties.getRootLib();
        log.info("Starting library scan: {}", rootPath);

        booksAdded.set(0);
        booksUpdated.set(0);
        booksSkipped.set(0);
        errors.set(0);

        long startTime = System.currentTimeMillis();

        Path root = Paths.get(rootPath);
        if (!Files.exists(root)) {
            log.error("Root library path does not exist: {}", rootPath);
            return ScanResult.builder()
                    .success(false)
                    .error("Root path does not exist: " + rootPath)
                    .build();
        }

        try {
            // Ensure root catalog exists
            Catalog rootCatalog = getOrCreateCatalog(root.getFileName().toString(), "/", null);

            // Scan directory tree
            scanDirectory(root, rootCatalog);

            // Update counters
            counterService.set("books", (int) bookRepository.countAvailable());
            counterService.set("authors", (int) authorRepository.count());
            counterService.set("catalogs", (int) catalogRepository.count());

            long duration = System.currentTimeMillis() - startTime;
            log.info("Scan completed in {}ms. Added: {}, Updated: {}, Skipped: {}, Errors: {}",
                    duration, booksAdded.get(), booksUpdated.get(), booksSkipped.get(), errors.get());

            return ScanResult.builder()
                    .success(true)
                    .booksAdded(booksAdded.get())
                    .booksUpdated(booksUpdated.get())
                    .booksSkipped(booksSkipped.get())
                    .errors(errors.get())
                    .durationMs(duration)
                    .build();

        } catch (Exception e) {
            log.error("Scan failed", e);
            return ScanResult.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    private void scanDirectory(Path directory, Catalog parentCatalog) throws IOException {
        List<String> extensions = properties.getBookExtensionsList();

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!dir.equals(directory)) {
                    String relativePath = directory.relativize(dir).toString().replace("\\", "/");
                    getOrCreateCatalog(dir.getFileName().toString(), "/" + relativePath, parentCatalog);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {
                    String filename = file.getFileName().toString().toLowerCase();

                    // Check if it's an archive
                    if (filename.endsWith(".zip")) {
                        processArchive(file, parentCatalog);
                    } else {
                        String extension = getExtension(filename);
                        if (extensions.contains(extension)) {
                            processBookFile(file, parentCatalog, false, null);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing file: {}", file, e);
                    errors.incrementAndGet();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                log.warn("Failed to access file: {}", file);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processArchive(Path archivePath, Catalog parentCatalog) {
        List<String> extensions = properties.getBookExtensionsList();

        try (ZipFile zipFile = ZipFile.builder().setPath(archivePath).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.isDirectory()) continue;

                String entryName = entry.getName();
                String extension = getExtension(entryName.toLowerCase());

                if (extensions.contains(extension)) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        processBookStream(is, entryName, entry.getSize(),
                                archivePath.toString(), parentCatalog);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing archive: {}", archivePath, e);
            errors.incrementAndGet();
        }
    }

    private void processBookFile(Path file, Catalog catalog, boolean inArchive, String archivePath) {
        try {
            String filename = file.getFileName().toString();
            long filesize = Files.size(file);

            try (InputStream is = Files.newInputStream(file)) {
                processBookStream(is, filename, filesize, archivePath, catalog);
            }
        } catch (Exception e) {
            log.error("Error processing book file: {}", file, e);
            errors.incrementAndGet();
        }
    }

    private void processBookStream(InputStream inputStream, String filename, long filesize,
                                   String archivePath, Catalog catalog) {
        String extension = getExtension(filename.toLowerCase());
        String path = archivePath != null ? archivePath + ":" + filename : filename;

        // Check if book already exists
        Optional<Book> existingBook = bookRepository.findByPath(path);
        if (existingBook.isPresent()) {
            booksSkipped.incrementAndGet();
            return;
        }

        // Find appropriate parser
        BookParser parser = parsers.stream()
                .filter(p -> p.supports(extension))
                .findFirst()
                .orElse(null);

        if (parser == null) {
            log.debug("No parser found for format: {}", extension);
            booksSkipped.incrementAndGet();
            return;
        }

        try {
            // Parse book metadata
            byte[] content = inputStream.readAllBytes();
            BookInfo bookInfo = parser.parse(new ByteArrayInputStream(content), filename);

            // Set file info
            bookInfo.setFilename(filename);
            bookInfo.setPath(path);
            bookInfo.setFormat(extension);
            bookInfo.setFilesize(filesize);
            bookInfo.setInArchive(archivePath != null);
            bookInfo.setArchivePath(archivePath);

            // Save to database
            saveBook(bookInfo, catalog);
            booksAdded.incrementAndGet();

        } catch (Exception e) {
            log.error("Error parsing book: {}", filename, e);
            errors.incrementAndGet();
        }
    }

    @Transactional
    protected void saveBook(BookInfo bookInfo, Catalog catalog) {
        // Get or create authors
        Set<Author> authors = new HashSet<>();
        for (AuthorInfo authorInfo : bookInfo.getAuthors()) {
            Author author = getOrCreateAuthor(authorInfo);
            authors.add(author);
        }

        // Get or create genres
        Set<Genre> genres = new HashSet<>();
        for (GenreInfo genreInfo : bookInfo.getGenres()) {
            Genre genre = getOrCreateGenre(genreInfo);
            genres.add(genre);
        }

        // Get or create series
        Set<Series> seriesSet = new HashSet<>();
        for (SeriesInfo seriesInfo : bookInfo.getSeries()) {
            Series series = getOrCreateSeries(seriesInfo);
            seriesSet.add(series);
        }

        // Create book
        Book book = Book.builder()
                .filename(bookInfo.getFilename())
                .path(bookInfo.getPath())
                .filesize((int) bookInfo.getFilesize())
                .format(bookInfo.getFormat())
                .title(bookInfo.getTitle())
                .searchTitle(bookInfo.getTitle().toLowerCase())
                .annotation(truncate(bookInfo.getAnnotation(), 10000))
                .lang(bookInfo.getLanguage())
                .docdate(bookInfo.getDocDate())
                .langCode(getLangCode(bookInfo.getLanguage()))
                .avail(2) // Available
                .catType(0)
                .catalog(catalog)
                .authors(authors)
                .genres(genres)
                .series(seriesSet)
                .registerdate(LocalDateTime.now())
                .build();

        bookRepository.save(book);
        log.debug("Saved book: {}", book.getTitle());
    }

    private Author getOrCreateAuthor(AuthorInfo authorInfo) {
        String fullName = authorInfo.getFullName();
        return authorRepository.findByFullName(fullName)
                .orElseGet(() -> {
                    Author author = Author.builder()
                            .fullName(fullName)
                            .searchFullName(fullName.toLowerCase())
                            .langCode(9)
                            .build();
                    return authorRepository.save(author);
                });
    }

    private Genre getOrCreateGenre(GenreInfo genreInfo) {
        return genreRepository.findByGenre(genreInfo.getGenre())
                .orElseGet(() -> {
                    Genre genre = Genre.builder()
                            .genre(genreInfo.getGenre())
                            .section(genreInfo.getSection() != null ? genreInfo.getSection() : "")
                            .subsection(genreInfo.getSubsection() != null ? genreInfo.getSubsection() : "")
                            .build();
                    return genreRepository.save(genre);
                });
    }

    private Series getOrCreateSeries(SeriesInfo seriesInfo) {
        return seriesRepository.findBySer(seriesInfo.getName())
                .orElseGet(() -> {
                    Series series = Series.builder()
                            .ser(seriesInfo.getName())
                            .searchSer(seriesInfo.getName().toLowerCase())
                            .langCode(9)
                            .build();
                    return seriesRepository.save(series);
                });
    }

    private Catalog getOrCreateCatalog(String name, String path, Catalog parent) {
        return catalogRepository.findByPath(path)
                .orElseGet(() -> {
                    Catalog catalog = Catalog.builder()
                            .catName(name)
                            .path(path)
                            .catType(0)
                            .catSize(0)
                            .parent(parent)
                            .build();
                    return catalogRepository.save(catalog);
                });
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    private Integer getLangCode(String language) {
        if (language == null) return 9;
        return switch (language.toLowerCase()) {
            case "ru", "rus" -> 0;
            case "en", "eng" -> 1;
            case "de", "deu", "ger" -> 2;
            case "fr", "fra", "fre" -> 3;
            case "es", "spa" -> 4;
            case "it", "ita" -> 5;
            case "uk", "ukr" -> 6;
            case "be", "bel" -> 7;
            case "pl", "pol" -> 8;
            default -> 9;
        };
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return null;
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength);
    }
}
