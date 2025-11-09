package com.sopds.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "series", indexes = {
        @Index(name = "idx_series_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(length = 500)
    private String nameSort;

    @OneToMany(mappedBy = "series", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Book> books = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Series)) return false;
        Series series = (Series) o;
        return id != null && id.equals(series.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}