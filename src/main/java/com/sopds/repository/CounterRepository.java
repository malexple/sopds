package com.sopds.repository;

import com.sopds.domain.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {

    Optional<Counter> findByName(String name);
}
