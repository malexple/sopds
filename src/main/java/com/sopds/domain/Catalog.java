package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opds_catalog_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cat_name", nullable = false, length = 128)
    private String catName;

    @Column(name = "path", nullable = false, length = 1000)
    private String path;

    @Column(name = "cat_type", nullable = false)
    @Builder.Default
    private Integer catType = 0;

    @Column(name = "cat_size")
    @Builder.Default
    private Integer catSize = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Catalog parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Catalog> children = new HashSet<>();

    @OneToMany(mappedBy = "catalog", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Book> books = new HashSet<>();
}
