package com.sopds.service;

import com.sopds.domain.Genre;
import com.sopds.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public Optional<Genre> getById(Long id) {
        return genreRepository.findById(id);
    }

    public Optional<Genre> getByGenre(String genre) {
        return genreRepository.findByGenre(genre);
    }

    public long count() {
        return genreRepository.count();
    }

    public List<Genre> getAll() {
        return genreRepository.findAllByOrderBySection();
    }

    public List<Object[]> getSections() {
        return genreRepository.getGenreSections();
    }

    public List<Object[]> getBySection(String section) {
        return genreRepository.getGenresBySection(section);
    }

    public Genre save(Genre genre) {
        return genreRepository.save(genre);
    }
}
