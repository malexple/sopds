package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opds_catalog_series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ser", nullable = false, length = 80)
    private String ser;

    @Column(name = "search_ser", nullable = false, length = 80)
    private String searchSer;

    @Column(name = "lang_code", nullable = false)
    @Builder.Default
    private Integer langCode = 9;

    @ManyToMany(mappedBy = "series", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Book> books = new HashSet<>();
}
