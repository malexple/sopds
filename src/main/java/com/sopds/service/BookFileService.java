package com.sopds.service;

import com.sopds.domain.Book;
import com.sopds.domain.BookFile;
import com.sopds.repository.BookFileRepository;
import com.sopds.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookFileService {

    private final BookFileRepository bookFileRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Optional<BookFile> getById(Long id) {
        return bookFileRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<BookFile> getByHash(String hash) {
        return bookFileRepository.findByHash(hash);
    }

    @Transactional(readOnly = true)
    public Optional<BookFile> getByPath(String path) {
        return bookFileRepository.findByPath(path);
    }

    @Transactional(readOnly = true)
    public List<BookFile> getByBookId(Long bookId) {
        return bookFileRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<BookFile> getByFileType(String fileType) {
        return bookFileRepository.findByFileType(fileType);
    }

    public BookFile create(Long bookId, String path, String fileType, Long size,
                           String hash, Boolean inArchive, String archivePath) {
        log.info("Creating book file for book ID: {}, path: {}", bookId, path);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        BookFile bookFile = BookFile.builder()
                .book(book)
                .path(path)
                .fileType(fileType)
                .size(size)
                .hash(hash)
                .inArchive(inArchive != null && inArchive)
                .archivePath(archivePath)
                .build();

        return bookFileRepository.save(bookFile);
    }

    public void delete(Long id) {
        log.info("Deleting book file ID: {}", id);
        bookFileRepository.deleteById(id);
    }
}
