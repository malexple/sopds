package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "opds_catalog_counter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer value;
}
