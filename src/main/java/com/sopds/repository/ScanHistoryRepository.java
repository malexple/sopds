package com.sopds.repository;

import com.sopds.domain.ScanHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScanHistoryRepository extends JpaRepository<ScanHistory, Long> {

    @Query("SELECT sh FROM ScanHistory sh WHERE sh.status = 'SUCCESS' ORDER BY sh.scanDate DESC")
    Optional<ScanHistory> findLastSuccessfulScan();

    Page<ScanHistory> findAllByOrderByScanDateDesc(Pageable pageable);

    List<ScanHistory> findByStatus(String status);

    long countByStatus(String status);
}
