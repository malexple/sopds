package com.sopds.catalog.repository;

import com.sopds.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByPath(String path);

    Optional<Book> findByMd5(String md5);

    @Query("SELECT COUNT(b) FROM Book b WHERE b.available = true")
    long countAvailableBooks();

    @Query("SELECT b FROM Book b WHERE b.path = :path AND b.filename = :filename")
    Optional<Book> findByPathAndFilename(@Param("path") String path, @Param("filename") String filename);

    boolean existsByPath(String path);
}