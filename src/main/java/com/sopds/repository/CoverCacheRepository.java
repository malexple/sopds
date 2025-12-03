package com.sopds.repository;

import com.sopds.domain.CoverCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoverCacheRepository extends JpaRepository<CoverCache, Long> {

    Optional<CoverCache> findByBookId(Long bookId);

    boolean existsByBookId(Long bookId);

    void deleteByBookId(Long bookId);
}
