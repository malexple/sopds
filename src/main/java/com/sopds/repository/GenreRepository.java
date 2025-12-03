package com.sopds.repository;

import com.sopds.domain.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    Optional<Genre> findByGenre(String genre);

    // Секции жанров с количеством книг
    @Query(value = """
        SELECT g.section, MIN(g.id) as section_id, COUNT(DISTINCT bg.book_id) as num_book
        FROM opds_catalog_genre g
        LEFT JOIN opds_catalog_bgenre bg ON g.id = bg.genre_id
        GROUP BY g.section
        HAVING COUNT(DISTINCT bg.book_id) > 0
        ORDER BY g.section
        """, nativeQuery = true)
    List<Object[]> getGenreSections();

    // Подсекции жанров для конкретной секции
    @Query(value = """
        SELECT g.id, g.genre, g.section, g.subsection, COUNT(DISTINCT bg.book_id) as num_book
        FROM opds_catalog_genre g
        LEFT JOIN opds_catalog_bgenre bg ON g.id = bg.genre_id
        WHERE g.section = :section
        GROUP BY g.id, g.genre, g.section, g.subsection
        HAVING COUNT(DISTINCT bg.book_id) > 0
        ORDER BY g.subsection
        """, nativeQuery = true)
    List<Object[]> getGenresBySection(@Param("section") String section);

    // Все жанры
    List<Genre> findAllByOrderBySection();
}
