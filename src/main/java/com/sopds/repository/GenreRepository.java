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

    Optional<Genre> findByGenre(String genre);

    @Query("SELECT DISTINCT g.section FROM Genre g ORDER BY g.section")
    List<String> findAllSections();

    @Query("SELECT g FROM Genre g WHERE g.section = :section ORDER BY g.subsection")
    List<Genre> findBySection(@Param("section") String section);

    @Query("SELECT g FROM Genre g JOIN g.books b WHERE b.id = :bookId")
    List<Genre> findByBookId(@Param("bookId") Long bookId);
}
