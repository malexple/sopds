package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scan_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scan_date", nullable = false)
    private LocalDateTime scanDate;

    @Column(name = "books_added", nullable = false)
    @Builder.Default
    private Integer booksAdded = 0;

    @Column(name = "books_updated", nullable = false)
    @Builder.Default
    private Integer booksUpdated = 0;

    @Column(name = "books_deleted", nullable = false)
    @Builder.Default
    private Integer booksDeleted = 0;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(length = 50)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (scanDate == null) {
            scanDate = LocalDateTime.now();
        }
    }
}
