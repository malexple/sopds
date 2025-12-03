package com.sopds.repository;

import com.sopds.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByTelegramId(Long telegramId);

    List<User> findAllByIsActiveTrue();

    List<User> findAllByIsAdminTrue();

    boolean existsByUsername(String username);

    boolean existsByTelegramId(Long telegramId);
}
