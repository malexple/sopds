package com.sopds.service;

import com.sopds.domain.Genre;
import com.sopds.repository.GenreRepository;
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
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<Genre> getTopLevelGenres() {
        log.debug("Fetching top-level genres");
        return genreRepository.findTopLevelGenres();
    }

    @Transactional(readOnly = true)
    public List<Genre> getChildGenres(Long parentId) {
        log.debug("Fetching child genres for parent ID: {}", parentId);
        return genreRepository.findByParentId(parentId);
    }

    @Transactional(readOnly = true)
    public Optional<Genre> getById(Long id) {
        return genreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Genre> getByCode(String code) {
        return genreRepository.findByCode(code);
    }

    public Genre create(String name, String code, Long parentId) {
        log.info("Creating genre: name={}, code={}", name, code);

        if (genreRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Genre with code '" + code + "' already exists");
        }

        Genre parent = parentId != null ? genreRepository.findById(parentId).orElse(null) : null;

        Genre genre = Genre.builder()
                .name(name)
                .code(code)
                .parent(parent)
                .build();

        return genreRepository.save(genre);
    }

    public Genre update(Long id, String name, String code) {
        log.info("Updating genre ID: {}", id);

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id));

        if (name != null) genre.setName(name);
        if (code != null) genre.setCode(code);

        return genreRepository.save(genre);
    }

    public void delete(Long id) {
        log.info("Deleting genre ID: {}", id);
        genreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public long count() {
        return genreRepository.count();
    }
}
