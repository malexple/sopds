package com.sopds.repository;

import com.sopds.domain.Bookshelf;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookshelfRepository extends JpaRepository<Bookshelf, Long> {

    @Query("SELECT bs FROM Bookshelf bs WHERE bs.userId = :userId ORDER BY bs.readtime DESC")
    Page<Bookshelf> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT bs FROM Bookshelf bs WHERE bs.userId = :userId AND bs.book.id = :bookId")
    Optional<Bookshelf> findByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);

    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
