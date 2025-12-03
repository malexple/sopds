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

    Optional<Author> findByFullName(String fullName);

    @Query("SELECT a FROM Author a WHERE LOWER(a.searchFullName) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY a.fullName")
    List<Author> searchByName(@Param("query") String query);

    @Query("SELECT a FROM Author a WHERE a.langCode = :langCode ORDER BY a.fullName")
    List<Author> findByLangCode(@Param("langCode") Integer langCode);

    @Query("SELECT a FROM Author a JOIN a.books b WHERE b.id = :bookId ORDER BY a.fullName")
    List<Author> findByBookId(@Param("bookId") Long bookId);

    Page<Author> findAllByOrderByFullNameAsc(Pageable pageable);
}
