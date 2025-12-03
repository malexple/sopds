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

    Optional<Book> findByFileHash(String fileHash);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) AND b.available = true")
    Page<Book> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.id = :genreId AND b.available = true ORDER BY b.title")
    Page<Book> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.id = :authorId AND b.available = true ORDER BY b.title")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.available = true ORDER BY b.createdAt DESC")
    Page<Book> findRecentBooks(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.language = :lang AND b.available = true ORDER BY b.title")
    Page<Book> findByLanguage(@Param("lang") String lang, Pageable pageable);

    long countByAvailableTrue();

    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN b.authors a
        WHERE b.available = true
        AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Book> search(@Param("query") String query, Pageable pageable);
}
