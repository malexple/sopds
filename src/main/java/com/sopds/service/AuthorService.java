package com.sopds.service;

import com.sopds.domain.Author;
import com.sopds.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public Optional<Author> getById(Long id) {
        return authorRepository.findById(id);
    }

    public Optional<Author> getByFullName(String fullName) {
        return authorRepository.findByFullName(fullName);
    }

    public long count() {
        return authorRepository.count();
    }

    public Page<Author> search(String query, int page, int size) {
        return authorRepository.searchByNameContains(query, PageRequest.of(page, size));
    }

    public Page<Author> getAll(int page, int size) {
        return authorRepository.findAllByOrderBySearchFullName(PageRequest.of(page, size));
    }

    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }
}
