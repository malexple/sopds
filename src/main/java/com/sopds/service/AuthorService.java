package com.sopds.service;

import com.sopds.domain.Author;
import com.sopds.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public Optional<Author> getById(Long id) {
        return authorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Author> getByFullName(String fullName) {
        return authorRepository.findByFullName(fullName);
    }

    @Transactional(readOnly = true)
    public List<Author> search(String query) {
        log.debug("Searching authors: {}", query);
        return authorRepository.searchByName(query);
    }

    @Transactional(readOnly = true)
    public List<Author> getByBookId(Long bookId) {
        return authorRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<Author> getByLangCode(Integer langCode) {
        return authorRepository.findByLangCode(langCode);
    }

    @Transactional(readOnly = true)
    public Page<Author> getAll(int page, int size) {
        return authorRepository.findAllByOrderByFullNameAsc(PageRequest.of(page, size));
    }

    public Author getOrCreate(String fullName, Integer langCode) {
        return authorRepository.findByFullName(fullName)
                .orElseGet(() -> {
                    log.info("Creating author: {}", fullName);
                    Author author = Author.builder()
                            .fullName(fullName)
                            .searchFullName(fullName.toLowerCase())
                            .langCode(langCode != null ? langCode : 9)
                            .build();
                    return authorRepository.save(author);
                });
    }

    public Author update(Long id, String fullName, Integer langCode) {
        log.info("Updating author ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + id));

        if (fullName != null) {
            author.setFullName(fullName);
            author.setSearchFullName(fullName.toLowerCase());
        }
        if (langCode != null) {
            author.setLangCode(langCode);
        }

        return authorRepository.save(author);
    }

    public void delete(Long id) {
        log.info("Deleting author ID: {}", id);
        authorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return authorRepository.count();
    }
}
