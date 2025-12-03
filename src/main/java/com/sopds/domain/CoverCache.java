package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cover_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", unique = true, nullable = false)
    private Long bookId;

    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
