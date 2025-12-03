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
    public Optional<Genre> getById(Long id) {
        return genreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Genre> getByGenre(String genre) {
        return genreRepository.findByGenre(genre);
    }

    @Transactional(readOnly = true)
    public List<String> getAllSections() {
        return genreRepository.findAllSections();
    }

    @Transactional(readOnly = true)
    public List<Genre> getBySection(String section) {
        return genreRepository.findBySection(section);
    }

    @Transactional(readOnly = true)
    public List<Genre> getByBookId(Long bookId) {
        return genreRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public Genre getOrCreate(String genre, String section, String subsection) {
        return genreRepository.findByGenre(genre)
                .orElseGet(() -> {
                    log.info("Creating genre: {}", genre);
                    Genre newGenre = Genre.builder()
                            .genre(genre)
                            .section(section != null ? section : "")
                            .subsection(subsection != null ? subsection : "")
                            .build();
                    return genreRepository.save(newGenre);
                });
    }

    public Genre update(Long id, String genre, String section, String subsection) {
        log.info("Updating genre ID: {}", id);

        Genre entity = genreRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + id));

        if (genre != null) entity.setGenre(genre);
        if (section != null) entity.setSection(section);
        if (subsection != null) entity.setSubsection(subsection);

        return genreRepository.save(entity);
    }

    public void delete(Long id) {
        log.info("Deleting genre ID: {}", id);
        genreRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return genreRepository.count();
    }
}
