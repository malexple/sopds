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
    public Optional<Author> getByName(String name) {
        return authorRepository.findByName(name);
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
    public Page<Author> getAll(int page, int size) {
        return authorRepository.findAllByOrderByNameAsc(PageRequest.of(page, size));
    }

    public Author getOrCreate(String name, String sortName, String lang) {
        return authorRepository.findByName(name)
                .orElseGet(() -> {
                    log.info("Creating author: {}", name);
                    Author author = Author.builder()
                            .name(name)
                            .sortName(sortName != null ? sortName : name)
                            .lang(lang)
                            .build();
                    return authorRepository.save(author);
                });
    }

    public Author update(Long id, String name, String sortName, String biography, String lang) {
        log.info("Updating author ID: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Author not found: " + id));

        if (name != null) author.setName(name);
        if (sortName != null) author.setSortName(sortName);
        if (biography != null) author.setBiography(biography);
        if (lang != null) author.setLang(lang);

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
