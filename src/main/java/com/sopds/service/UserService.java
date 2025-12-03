package com.sopds.service;

import com.sopds.domain.User;
import com.sopds.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> getByTelegramId(Long telegramId) {
        return userRepository.findByTelegramId(telegramId);
    }

    public User create(String username, String password, Boolean isAdmin) {
        log.info("Creating user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .isAdmin(isAdmin != null && isAdmin)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public void updatePassword(Long userId, String newPassword) {
        log.info("Updating password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .orElse(false);
    }

    public User linkTelegram(Long userId, Long telegramId) {
        log.info("Linking Telegram ID {} to user ID: {}", telegramId, userId);

        if (userRepository.existsByTelegramId(telegramId)) {
            throw new IllegalArgumentException("Telegram ID already linked");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setTelegramId(telegramId);
        return userRepository.save(user);
    }

    public void setActive(Long userId, Boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsActive(active);
        userRepository.save(user);
    }

    public void setAdmin(Long userId, Boolean isAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsAdmin(isAdmin);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> getActiveUsers() {
        return userRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<User> getAdmins() {
        return userRepository.findAllByIsAdminTrue();
    }

    public void delete(Long id) {
        log.info("Deleting user ID: {}", id);
        userRepository.deleteById(id);
    }
}
