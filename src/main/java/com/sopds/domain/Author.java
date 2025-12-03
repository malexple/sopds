package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "sort_name", length = 255)
    private String sortName;

    @Column(length = 10)
    private String lang;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Book> books = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
