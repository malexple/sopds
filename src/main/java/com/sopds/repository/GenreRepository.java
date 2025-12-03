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

    Optional<Genre> findByName(String name);

    Optional<Genre> findByCode(String code);

    @Query("SELECT g FROM Genre g WHERE g.parent IS NULL ORDER BY g.name")
    List<Genre> findTopLevelGenres();

    @Query("SELECT g FROM Genre g WHERE g.parent.id = :parentId ORDER BY g.name")
    List<Genre> findByParentId(@Param("parentId") Long parentId);

    boolean existsByCode(String code);
}
