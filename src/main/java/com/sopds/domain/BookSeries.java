package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "opds_catalog_bseries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ser_no", nullable = false)
    @Builder.Default
    private Integer serNo = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ser_id", nullable = false)
    private Series ser;
}
