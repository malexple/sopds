package com.sopds.repository;

import com.sopds.domain.BookFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookFileRepository extends JpaRepository<BookFile, Long> {

    Optional<BookFile> findByHash(String hash);

    Optional<BookFile> findByPath(String path);

    @Query("SELECT bf FROM BookFile bf WHERE bf.book.id = :bookId")
    List<BookFile> findByBookId(@Param("bookId") Long bookId);

    @Query("SELECT bf FROM BookFile bf WHERE bf.fileType = :fileType")
    List<BookFile> findByFileType(@Param("fileType") String fileType);

    @Query("SELECT bf FROM BookFile bf WHERE bf.inArchive = true")
    List<BookFile> findAllInArchives();
}
