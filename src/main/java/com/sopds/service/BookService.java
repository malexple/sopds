package com.sopds.service;

import com.sopds.domain.Book;
import com.sopds.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Optional<Book> getById(Long id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> getByPath(String path) {
        return bookRepository.findByPath(path);
    }

    public long countAvailable() {
        return bookRepository.countAvailable();
    }

    public Page<Book> search(String query, int page, int size) {
        return bookRepository.searchByTitleContains(query, PageRequest.of(page, size));
    }

    public Page<Book> searchByTitle(String query, int page, int size) {
        return bookRepository.searchByTitleContains(query, PageRequest.of(page, size));
    }

    public Page<Book> getByAuthor(Long authorId, int page, int size) {
        return bookRepository.findByAuthorId(authorId, PageRequest.of(page, size));
    }

    public Page<Book> getByGenre(Long genreId, int page, int size) {
        return bookRepository.findByGenreId(genreId, PageRequest.of(page, size));
    }

    public Page<Book> getBySeries(Long seriesId, int page, int size) {
        return bookRepository.findBySeriesId(seriesId, PageRequest.of(page, size));
    }

    public Page<Book> getRecent(int page, int size) {
        return bookRepository.findRecent(PageRequest.of(page, size));
    }

    public List<Book> getByCatalogId(Long catalogId) {
        return bookRepository.findByCatalogId(catalogId);
    }

    public Book save(Book book) {
        return bookRepository.save(book);
    }
}
