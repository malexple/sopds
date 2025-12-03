package com.sopds.repository;

import com.sopds.domain.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByName(String name);

    Optional<Author> findBySortName(String sortName);

    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY a.name")
    List<Author> searchByName(@Param("query") String query);

    @Query("SELECT a FROM Author a WHERE a.lang = :lang ORDER BY a.name")
    List<Author> findByLang(@Param("lang") String lang);

    @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId ORDER BY a.name")
    List<Author> findByBookId(@Param("bookId") Long bookId);

    Page<Author> findAllByOrderByNameAsc(Pageable pageable);
}
