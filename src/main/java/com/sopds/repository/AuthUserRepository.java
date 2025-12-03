package com.sopds.repository;

import com.sopds.domain.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);

    List<AuthUser> findAllByIsActiveTrue();

    List<AuthUser> findAllByIsSuperuserTrue();
}
