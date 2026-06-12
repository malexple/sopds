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
            Catalog rootCatalog = getOrCreateCatalog(root.getFileName().toString(), "/", null);
            scanDirectory(root, root, rootCatalog);

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

    // Добавили параметр root — корень библиотеки для вычисления относительных путей
    private void scanDirectory(Path root, Path directory, Catalog parentCatalog) throws IOException {
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

                    if (filename.endsWith(".zip")) {
                        processArchive(file, parentCatalog);
                    } else {
                        String extension = getExtension(filename);
                        if (extensions.contains(extension)) {
                            processBookFile(file, root, parentCatalog);
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
                        processBookStream(is, entryName, entry.getSize(), archivePath.toString(), parentCatalog, entryName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing archive: {}", archivePath, e);
            errors.incrementAndGet();
        }
    }

    // Убрали inArchive/archivePath, добавили root для вычисления пути
    private void processBookFile(Path file, Path root, Catalog catalog) {
        try {
            String filename = file.getFileName().toString();
            long filesize = Files.size(file);
            // Относительный путь от rootLib: "rust/mybook.pdf"
            String relativePath = root.relativize(file).toString().replace("\\", "/");

            try (InputStream is = Files.newInputStream(file)) {
                processBookStream(is, filename, filesize, null, catalog, relativePath);
            }
        } catch (Exception e) {
            log.error("Error processing book file: {}", file, e);
            errors.incrementAndGet();
        }
    }

    private void processBookStream(InputStream inputStream, String filename, long filesize,
                                   String archivePath, Catalog catalog, String relativePath) {
        String extension = getExtension(filename.toLowerCase());
        // Для архивов: "archive.zip:filename", для файлов: "rust/mybook.pdf"
        String path = archivePath != null ? archivePath + ":" + filename : relativePath;

        Optional<Book> existingBook = bookRepository.findByPath(path);
        if (existingBook.isPresent()) {
            booksSkipped.incrementAndGet();
            return;
        }

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
            byte[] content = inputStream.readAllBytes();
            BookInfo bookInfo = parser.parse(new ByteArrayInputStream(content), filename);

            bookInfo.setFilename(filename);
            bookInfo.setPath(path);
            bookInfo.setFormat(extension);
            bookInfo.setFilesize(filesize);
            bookInfo.setInArchive(archivePath != null);
            bookInfo.setArchivePath(archivePath);

            saveBook(bookInfo, catalog);
            booksAdded.incrementAndGet();

        } catch (Exception e) {
            log.error("Error parsing book: {}", filename, e);
            errors.incrementAndGet();
        }
    }

    @Transactional
    protected void saveBook(BookInfo bookInfo, Catalog catalog) {
        Set<Author> authors = new HashSet<>();
        for (AuthorInfo authorInfo : bookInfo.getAuthors()) {
            authors.add(getOrCreateAuthor(authorInfo));
        }

        Set<Genre> genres = new HashSet<>();
        for (GenreInfo genreInfo : bookInfo.getGenres()) {
            genres.add(getOrCreateGenre(genreInfo));
        }

        Set<Series> seriesSet = new HashSet<>();
        for (SeriesInfo seriesInfo : bookInfo.getSeries()) {
            seriesSet.add(getOrCreateSeries(seriesInfo));
        }

        Book book = Book.builder()
                .filename(bookInfo.getFilename())
                .path(bookInfo.getPath())
                .filesize((int) bookInfo.getFilesize())
                .format(bookInfo.getFormat())
                .title(bookInfo.getTitle().trim())
                .searchTitle(bookInfo.getTitle().trim().toLowerCase())
                .annotation(truncate(bookInfo.getAnnotation(), 10000))
                .lang(bookInfo.getLanguage())
                .docdate(bookInfo.getDocDate())
                .langCode(getLangCode(bookInfo.getLanguage()))
                .avail(2)
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
                .orElseGet(() -> authorRepository.save(Author.builder()
                        .fullName(fullName)
                        .searchFullName(fullName.toLowerCase())
                        .langCode(9)
                        .build()));
    }

    private Genre getOrCreateGenre(GenreInfo genreInfo) {
        return genreRepository.findByGenre(genreInfo.getGenre())
                .orElseGet(() -> genreRepository.save(Genre.builder()
                        .genre(genreInfo.getGenre())
                        .section(genreInfo.getSection() != null ? genreInfo.getSection() : "")
                        .subsection(genreInfo.getSubsection() != null ? genreInfo.getSubsection() : "")
                        .build()));
    }

    private Series getOrCreateSeries(SeriesInfo seriesInfo) {
        return seriesRepository.findBySer(seriesInfo.getName())
                .orElseGet(() -> seriesRepository.save(Series.builder()
                        .ser(seriesInfo.getName())
                        .searchSer(seriesInfo.getName().toLowerCase())
                        .langCode(9)
                        .build()));
    }

    private Catalog getOrCreateCatalog(String name, String path, Catalog parent) {
        return catalogRepository.findByPath(path)
                .orElseGet(() -> catalogRepository.save(Catalog.builder()
                        .catName(name)
                        .path(path)
                        .catType(0)
                        .catSize(0)
                        .parent(parent)
                        .build()));
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
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }
}