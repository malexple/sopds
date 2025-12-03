package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opds_catalog_book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false, length = 256)
    private String filename;

    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    @Column(name = "filesize", nullable = false)
    @Builder.Default
    private Integer filesize = 0;

    @Column(name = "format", nullable = false, length = 8)
    private String format;

    @Column(name = "cat_type", nullable = false)
    @Builder.Default
    private Integer catType = 0;

    @Column(name = "registerdate", nullable = false)
    private LocalDateTime registerdate;

    @Column(name = "docdate", length = 32)
    private String docdate;

    @Column(name = "lang", length = 16)
    private String lang;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "search_title", nullable = false, length = 256)
    private String searchTitle;

    @Column(name = "annotation", length = 10000)
    private String annotation;

    @Column(name = "lang_code", nullable = false)
    @Builder.Default
    private Integer langCode = 9;

    @Column(name = "avail", nullable = false)
    @Builder.Default
    private Integer avail = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_id", nullable = false)
    private Catalog catalog;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "opds_catalog_bauthor",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    @Builder.Default
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "opds_catalog_bgenre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "opds_catalog_bseries",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "ser_id")
    )
    @Builder.Default
    private Set<Series> series = new HashSet<>();

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<BookSeries> bookSeries = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (registerdate == null) {
            registerdate = LocalDateTime.now();
        }
    }
}
