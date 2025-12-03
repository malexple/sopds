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

    @Query("SELECT s FROM Series s WHERE LOWER(s.searchSer) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY s.ser")
    List<Series> searchBySer(@Param("query") String query);

    @Query("SELECT s FROM Series s WHERE s.langCode = :langCode ORDER BY s.ser")
    List<Series> findByLangCode(@Param("langCode") Integer langCode);

    Page<Series> findAllByOrderBySerAsc(Pageable pageable);
}
