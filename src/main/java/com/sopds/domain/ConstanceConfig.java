package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "constance_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConstanceConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key", unique = true, nullable = false, length = 255)
    private String key;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;
}
