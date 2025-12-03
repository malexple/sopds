package com.sopds.service;

import com.sopds.domain.Book;
import com.sopds.domain.Bookshelf;
import com.sopds.repository.BookRepository;
import com.sopds.repository.BookshelfRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookshelfService {

    private final BookshelfRepository bookshelfRepository;
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<Bookshelf> getByUserId(Long userId, int page, int size) {
        return bookshelfRepository.findByUserId(userId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public Optional<Bookshelf> getByUserIdAndBookId(Long userId, Long bookId) {
        return bookshelfRepository.findByUserIdAndBookId(userId, bookId);
    }

    public Bookshelf addToShelf(Long userId, Long bookId) {
        log.info("Adding book {} to shelf for user {}", bookId, userId);

        Optional<Bookshelf> existing = bookshelfRepository.findByUserIdAndBookId(userId, bookId);
        if (existing.isPresent()) {
            Bookshelf shelf = existing.get();
            shelf.setReadtime(LocalDateTime.now());
            return bookshelfRepository.save(shelf);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + bookId));

        Bookshelf bookshelf = Bookshelf.builder()
                .userId(userId)
                .book(book)
                .readtime(LocalDateTime.now())
                .build();

        return bookshelfRepository.save(bookshelf);
    }

    public void removeFromShelf(Long userId, Long bookId) {
        log.info("Removing book {} from shelf for user {}", bookId, userId);
        bookshelfRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    @Transactional(readOnly = true)
    public boolean isOnShelf(Long userId, Long bookId) {
        return bookshelfRepository.findByUserIdAndBookId(userId, bookId).isPresent();
    }
}
