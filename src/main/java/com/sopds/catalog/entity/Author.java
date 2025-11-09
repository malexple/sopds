package com.sopds.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authors", indexes = {
        @Index(name = "idx_author_full_name", columnList = "fullName"),
        @Index(name = "idx_author_last_name", columnList = "lastName")
})
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
    private String fullName;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String middleName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 255)
    private String fullNameSort;

    // Many-to-Many with Books
    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Book> books = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Author)) return false;
        Author author = (Author) o;
        return id != null && id.equals(author.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}