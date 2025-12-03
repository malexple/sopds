package com.sopds.repository;

import com.sopds.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByFilename(String filename);

    Optional<Book> findByPath(String path);

    @Query("SELECT b FROM Book b WHERE LOWER(b.searchTitle) LIKE LOWER(CONCAT('%', :query, '%')) AND b.avail = 2")
    Page<Book> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.id = :genreId AND b.avail = 2 ORDER BY b.title")
    Page<Book> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId AND b.avail = 2 ORDER BY b.title")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.series s WHERE s.id = :seriesId AND b.avail = 2 ORDER BY b.title")
    Page<Book> findBySeriesId(@Param("seriesId") Long seriesId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.avail = 2 ORDER BY b.registerdate DESC")
    Page<Book> findRecentBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.catalog.id = :catalogId AND b.avail = 2 ORDER BY b.title")
    Page<Book> findByCatalogId(@Param("catalogId") Long catalogId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.langCode = :langCode AND b.avail = 2 ORDER BY b.title")
    Page<Book> findByLangCode(@Param("langCode") Integer langCode, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.avail = 2")
    long countAvailable();

    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN b.authors a
        WHERE b.avail = 2
        AND (LOWER(b.searchTitle) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(a.searchFullName) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Book> search(@Param("query") String query, Pageable pageable);
}
