package com.sopds.repository;

import com.sopds.domain.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    Optional<Series> findBySer(String ser);

    // Поиск по названию (contains)
    @Query("SELECT s FROM Series s WHERE s.searchSer LIKE CONCAT('%', :term, '%') ORDER BY s.searchSer")
    Page<Series> searchByNameContains(@Param("term") String term, Pageable pageable);

    // Поиск по названию (startsWith)
    @Query("SELECT s FROM Series s WHERE s.searchSer LIKE CONCAT(:term, '%') ORDER BY s.searchSer")
    Page<Series> searchByNameStartsWith(@Param("term") String term, Pageable pageable);

    // Поиск по точному совпадению
    @Query("SELECT s FROM Series s WHERE s.searchSer = :term ORDER BY s.searchSer")
    Page<Series> searchByNameExact(@Param("term") String term, Pageable pageable);

    // Все серии с пагинацией
    Page<Series> findAllByOrderBySearchSer(Pageable pageable);

    // Группировка по первым символам
    @Query(value = """
        SELECT SUBSTRING(search_ser, 1, :length) as id, COUNT(*) as cnt 
        FROM opds_catalog_series 
        WHERE (:langCode = 0 OR lang_code = :langCode) 
          AND search_ser LIKE :chars || '%'
        GROUP BY SUBSTRING(search_ser, 1, :length) 
        ORDER BY id
        """, nativeQuery = true)
    List<Object[]> getSeriesPrefixes(@Param("length") int length, @Param("langCode") int langCode, @Param("chars") String chars);
}
