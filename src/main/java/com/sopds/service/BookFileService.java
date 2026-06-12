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

    public InputStream getBookInputStream(Book book) throws IOException {
        Path rootPath = Paths.get(properties.getRootLib()).toAbsolutePath().normalize();
        String bookPath = book.getPath();

        // Ищем ":" только начиная с позиции 2 — чтобы не спутать с диском "d:"
        int sep = bookPath.indexOf(':', 2);

        if (sep > 0) {
            // Это архив: "subdir/archive.zip:book.fb2"
            Path archivePath = rootPath.resolve(bookPath.substring(0, sep));
            String entryName = bookPath.substring(sep + 1);
            log.info("Reading from ZIP: {} -> {}", archivePath, entryName);
            return getInputStreamFromZip(archivePath, entryName);
        }

        // Обычный файл
        Path filePath = rootPath.resolve(bookPath);
        log.info("Reading file: {}", filePath);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Book not found: " + filePath);
        }
        return Files.newInputStream(filePath);
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
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) return entry;

        var entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (e.isDirectory()) continue;
            String name = e.getName();
            int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
            String nameOnly = slash >= 0 ? name.substring(slash + 1) : name;
            if (nameOnly.equalsIgnoreCase(entryName)) return e;
        }

        entries = zipFile.entries();
        ZipEntry single = null;
        int count = 0;
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.isDirectory()) { single = e; count++; }
        }
        return count == 1 ? single : null;
    }
}