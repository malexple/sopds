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

    // Поиск по имени (contains)
    @Query("SELECT a FROM Author a WHERE UPPER(a.searchFullName) LIKE UPPER(CONCAT('%', :term, '%')) ORDER BY a.searchFullName")
    Page<Author> searchByNameContains(@Param("term") String term, Pageable pageable);

    // Поиск по имени (startsWith)
    @Query("SELECT a FROM Author a WHERE UPPER(a.searchFullName) LIKE UPPER(CONCAT(:term, '%')) ORDER BY a.searchFullName")
    Page<Author> searchByNameStartsWith(@Param("term") String term, Pageable pageable);

    // Поиск по точному совпадению
    @Query("SELECT a FROM Author a WHERE UPPER(a.searchFullName) = UPPER(:term) ORDER BY a.searchFullName")
    Page<Author> searchByNameExact(@Param("term") String term, Pageable pageable);

    // Все авторы с пагинацией
    Page<Author> findAllByOrderBySearchFullName(Pageable pageable);

    // Группировка по первым символам для алфавитного меню
    @Query(value = """
        SELECT SUBSTRING(search_full_name, 1, :length) as id, COUNT(*) as cnt 
        FROM opds_catalog_author 
        WHERE (:langCode = 0 OR lang_code = :langCode) 
          AND search_full_name LIKE :chars || '%'
        GROUP BY SUBSTRING(search_full_name, 1, :length) 
        ORDER BY id
        """, nativeQuery = true)
    List<Object[]> getAuthorPrefixes(@Param("length") int length, @Param("langCode") int langCode, @Param("chars") String chars);
}
