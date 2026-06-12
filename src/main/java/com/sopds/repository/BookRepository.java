package com.sopds.repository;

import com.sopds.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByPath(String path);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.avail = 2")
    long countAvailable();

    // Поиск по названию (contains)
    @Query("SELECT b FROM Book b WHERE b.avail = 2 AND b.searchTitle LIKE CONCAT('%', :term, '%') ORDER BY b.searchTitle, b.docdate DESC")
    Page<Book> searchByTitleContains(@Param("term") String term, Pageable pageable);

    // Поиск по названию (startsWith)
    @Query("SELECT b FROM Book b WHERE b.avail = 2 AND b.searchTitle LIKE CONCAT(:term, '%') ORDER BY b.searchTitle, b.docdate DESC")
    Page<Book> searchByTitleStartsWith(@Param("term") String term, Pageable pageable);

    // Книги автора
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE b.avail = 2 AND a.id = :authorId ORDER BY b.searchTitle, b.docdate DESC")
    Page<Book> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    // Книги серии
    @Query("SELECT b FROM Book b JOIN b.series s WHERE b.avail = 2 AND s.id = :seriesId ORDER BY b.searchTitle, b.docdate DESC")
    Page<Book> findBySeriesId(@Param("seriesId") Long seriesId, Pageable pageable);

    // Книги жанра
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE b.avail = 2 AND g.id = :genreId ORDER BY b.searchTitle, b.docdate DESC")
    Page<Book> findByGenreId(@Param("genreId") Long genreId, Pageable pageable);

    // Книги каталога
    @Query("SELECT b FROM Book b WHERE b.avail = 2 AND b.catalog.id = :catalogId ORDER BY b.searchTitle")
    List<Book> findByCatalogId(@Param("catalogId") Long catalogId);

    // Стало (JPQL + Pageable, работает везде):
    @Query("SELECT b FROM Book b WHERE b.avail = 2")
    default Book findRandomBook(int offset) {
        return findRandomBookPage(PageRequest.of(offset, 1))
                .stream().findFirst().orElse(null);
    }

    @Query("SELECT b FROM Book b WHERE b.avail = 2")
    List<Book> findRandomBookPage(Pageable pageable);

    // Последние добавленные
    @Query("SELECT b FROM Book b WHERE b.avail = 2 ORDER BY b.registerdate DESC")
    Page<Book> findRecent(Pageable pageable);

    // Поиск дубликатов
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE b.avail = 2 AND b.title = :title AND a.id IN :authorIds AND b.id <> :excludeId ORDER BY b.docdate DESC")
    List<Book> findDoubles(@Param("title") String title, @Param("authorIds") List<Long> authorIds, @Param("excludeId") Long excludeId);

    @Query("SELECT b FROM Book b WHERE b.avail = 2")
    List<Book> findAvailableBooks(Pageable pageable);
}
