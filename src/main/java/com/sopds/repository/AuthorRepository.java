package com.sopds.repository;

import com.sopds.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    Optional<Author> findByFullName(String fullName);

    Optional<Author> findByFirstNameAndMiddleNameAndLastName(
            String firstName,
            String middleName,
            String lastName
    );
}