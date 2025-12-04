package com.sopds.service;

import com.sopds.config.SopdsProperties;
import com.sopds.domain.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookFileService {

    private final SopdsProperties properties;

    /**
     * Получает InputStream для чтения книги
     */
    public InputStream getBookInputStream(Book book) throws IOException {
        String filename = book.getFilename();

        // Получаем rootLib из настроек
        String rootLib = properties.getRootLib();
        if (rootLib == null || rootLib.isEmpty()) {
            throw new IOException("rootLib is not configured in application.yml");
        }

        Path rootPath = Paths.get(rootLib).toAbsolutePath().normalize();
        log.info("RootLib: {}, filename: {}", rootPath, filename);

        // Получаем относительный путь из книги
        String bookPath = book.getPath();
        String catalogPath = book.getCatalog() != null ? book.getCatalog().getPath() : null;

        // Нормализуем пути (убираем ".")
        bookPath = normalizePath(bookPath);
        catalogPath = normalizePath(catalogPath);

        log.debug("Normalized - catalogPath: '{}', bookPath: '{}'", catalogPath, bookPath);

        // Формируем полный путь
        Path basePath = rootPath;
        if (!catalogPath.isEmpty()) {
            basePath = basePath.resolve(catalogPath);
        }
        if (!bookPath.isEmpty() && !isArchiveOrFile(bookPath)) {
            basePath = basePath.resolve(bookPath);
        }

        log.info("Looking for book in: {}", basePath);

        // 1. Прямой файл
        Path directFile = basePath.resolve(filename);
        log.debug("Try 1 - direct: {}", directFile);
        if (Files.exists(directFile) && Files.isRegularFile(directFile)) {
            log.info("Found: {}", directFile);
            return Files.newInputStream(directFile);
        }

        // 2. ZIP с таким же именем
        Path zipFile = basePath.resolve(filename + ".zip");
        log.debug("Try 2 - zip: {}", zipFile);
        if (Files.exists(zipFile)) {
            log.info("Found ZIP: {}", zipFile);
            return getInputStreamFromZip(zipFile, filename);
        }

        // 3. bookPath — это архив
        if (!bookPath.isEmpty() && bookPath.toLowerCase().endsWith(".zip")) {
            Path archivePath = rootPath.resolve(catalogPath.isEmpty() ? bookPath : catalogPath + "/" + bookPath);
            log.debug("Try 3 - bookPath is archive: {}", archivePath);
            if (Files.exists(archivePath)) {
                log.info("Found archive: {}", archivePath);
                return getInputStreamFromZip(archivePath, filename);
            }
        }

        // 4. Поиск в rootPath напрямую (если path был ".")
        Path rootDirect = rootPath.resolve(filename);
        log.debug("Try 4 - root direct: {}", rootDirect);
        if (Files.exists(rootDirect) && Files.isRegularFile(rootDirect)) {
            log.info("Found in root: {}", rootDirect);
            return Files.newInputStream(rootDirect);
        }

        // 5. ZIP в rootPath
        Path rootZip = rootPath.resolve(filename + ".zip");
        log.debug("Try 5 - root zip: {}", rootZip);
        if (Files.exists(rootZip)) {
            log.info("Found ZIP in root: {}", rootZip);
            return getInputStreamFromZip(rootZip, filename);
        }

        throw new FileNotFoundException("Book not found: " + filename + "\nTried:\n" +
                "  - " + directFile + "\n" +
                "  - " + zipFile + "\n" +
                "  - " + rootDirect + "\n" +
                "  - " + rootZip);
    }

    private String normalizePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return "";
        }
        path = path.trim();
        if (path.equals(".") || path.equals("./") || path.equals(".\\")) {
            return "";
        }
        if (path.startsWith("./") || path.startsWith(".\\")) {
            path = path.substring(2);
        }
        return path;
    }

    private boolean isArchiveOrFile(String path) {
        String lower = path.toLowerCase();
        return lower.endsWith(".zip") || lower.endsWith(".rar") ||
                lower.endsWith(".7z") || lower.endsWith(".fb2") ||
                lower.endsWith(".epub") || lower.endsWith(".pdf");
    }

    private InputStream getInputStreamFromZip(Path zipPath, String entryName) throws IOException {
        ZipFile zipFile = new ZipFile(zipPath.toFile());
        ZipEntry entry = findZipEntry(zipFile, entryName);

        if (entry == null) {
            zipFile.close();
            throw new FileNotFoundException("Entry '" + entryName + "' not found in: " + zipPath);
        }

        InputStream zipInputStream = zipFile.getInputStream(entry);
        return new FilterInputStream(zipInputStream) {
            @Override
            public void close() throws IOException {
                super.close();
                zipFile.close();
            }
        };
    }

    private ZipEntry findZipEntry(ZipFile zipFile, String entryName) {
        // Точное совпадение
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) return entry;

        // По имени файла
        var entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (e.isDirectory()) continue;

            String name = e.getName();
            int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            String nameOnly = slash >= 0 ? name.substring(slash + 1) : name;

            if (nameOnly.equalsIgnoreCase(entryName)) {
                return e;
            }
        }

        // Единственный файл
        entries = zipFile.entries();
        ZipEntry single = null;
        int count = 0;
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.isDirectory()) {
                single = e;
                count++;
            }
        }
        return count == 1 ? single : null;
    }

    public byte[] getBookBytes(Book book) throws IOException {
        try (InputStream is = getBookInputStream(book)) {
            return is.readAllBytes();
        }
    }

    public boolean bookFileExists(Book book) {
        try {
            getBookInputStream(book).close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
