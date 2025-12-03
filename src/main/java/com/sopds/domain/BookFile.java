package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, length = 1000)
    private String path;

    @Column(name = "file_type", length = 10)
    private String fileType;

    @Column(nullable = false)
    private Long size;

    @Column(length = 64)
    private String hash;

    @Column(name = "in_archive", nullable = false)
    @Builder.Default
    private Boolean inArchive = false;

    @Column(name = "archive_path", length = 1000)
    private String archivePath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
