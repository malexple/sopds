package com.sopds.service;

import com.sopds.domain.Author;
import com.sopds.domain.Book;
import com.sopds.domain.Catalog;
import com.sopds.domain.Genre;
import com.sopds.repository.AuthorRepository;
import com.sopds.repository.BookRepository;
import com.sopds.repository.CatalogRepository;
import com.sopds.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private final CatalogRepository catalogRepository;

    @Transactional(readOnly = true)
    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
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
    public Page<Book> getBySeries(Long seriesId, int page, int size) {
        return bookRepository.findBySeriesId(seriesId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getRecent(int page, int size) {
        return bookRepository.findRecentBooks(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Book> getByCatalog(Long catalogId, int page, int size) {
        return bookRepository.findByCatalogId(catalogId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public long countAvailable() {
        return bookRepository.countAvailable();
    }

    public Book create(String filename, String path, Integer filesize, String format,
                       String title, String annotation, Long catalogId,
                       Set<Long> authorIds, Set<Long> genreIds) {
        log.info("Creating book: {}", title);

        Catalog catalog = catalogRepository.findById(catalogId)
                .orElseThrow(() -> new IllegalArgumentException("Catalog not found: " + catalogId));

        Set<Author> authors = new HashSet<>();
        if (authorIds != null) {
            authorIds.forEach(id -> authorRepository.findById(id).ifPresent(authors::add));
        }

        Set<Genre> genres = new HashSet<>();
        if (genreIds != null) {
            genreIds.forEach(id -> genreRepository.findById(id).ifPresent(genres::add));
        }

        Book book = Book.builder()
                .filename(filename)
                .path(path)
                .filesize(filesize != null ? filesize : 0)
                .format(format)
                .title(title)
                .searchTitle(title.toLowerCase())
                .annotation(annotation)
                .catalog(catalog)
                .authors(authors)
                .genres(genres)
                .avail(2) // 2 = available in Django SOPDS
                .registerdate(LocalDateTime.now())
                .build();

        return bookRepository.save(book);
    }

    public void setAvailable(Long id, int avail) {
        log.info("Setting book ID: {} avail: {}", id, avail);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

        book.setAvail(avail);
        bookRepository.save(book);
    }

    public void delete(Long id) {
        log.info("Deleting book ID: {}", id);
        bookRepository.deleteById(id);
    }
}
