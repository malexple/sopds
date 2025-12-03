package com.sopds.service;

import com.sopds.domain.Author;
import com.sopds.domain.Book;
import com.sopds.domain.Genre;
import com.sopds.repository.AuthorRepository;
import com.sopds.repository.BookRepository;
import com.sopds.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Book> getByFileHash(String fileHash) {
        return bookRepository.findByFileHash(fileHash);
    }

    @Transactional(readOnly = true)
    public Page<Book> searchByTitle(String query, int page, int size) {
        log.debug("Searching books by title: {}", query);
        return bookRepository.searchByTitle(query, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String query, int page, int size) {
        log.debug("Searching books: {}", query);
        return bookRepository.search(query, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getByGenre(Long genreId, int page, int size) {
        return bookRepository.findByGenreId(genreId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getByAuthor(Long authorId, int page, int size) {
        return bookRepository.findByAuthorId(authorId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getRecent(int page, int size) {
        return bookRepository.findRecentBooks(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getByLanguage(String lang, int page, int size) {
        return bookRepository.findByLanguage(lang, PageRequest.of(page, size));
    }

    public Book create(String title, String annotation, String language,
                       String pubDate, String fileHash,
                       Set<Long> authorIds, Set<Long> genreIds) {
        log.info("Creating book: {}", title);

        Set<Author> authors = new HashSet<>();
        if (authorIds != null) {
            authorIds.forEach(id -> authorRepository.findById(id).ifPresent(authors::add));
        }

        Set<Genre> genres = new HashSet<>();
        if (genreIds != null) {
            genreIds.forEach(id -> genreRepository.findById(id).ifPresent(genres::add));
        }

        Book book = Book.builder()
                .title(title)
                .annotation(annotation)
                .language(language)
                .pubDate(pubDate)
                .fileHash(fileHash)
                .available(true)
                .authors(authors)
                .genres(genres)
                .build();

        return bookRepository.save(book);
    }

    public Book update(Long id, String title, String annotation, String language, String pubDate) {
        log.info("Updating book ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

        if (title != null) book.setTitle(title);
        if (annotation != null) book.setAnnotation(annotation);
        if (language != null) book.setLanguage(language);
        if (pubDate != null) book.setPubDate(pubDate);

        return bookRepository.save(book);
    }

    public void setAvailable(Long id, boolean available) {
        log.info("Setting book ID: {} available: {}", id, available);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

        book.setAvailable(available);
        bookRepository.save(book);
    }

    public void delete(Long id) {
        log.info("Deleting book ID: {}", id);
        bookRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return bookRepository.countByAvailableTrue();
    }
}
