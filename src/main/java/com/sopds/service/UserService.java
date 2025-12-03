package com.sopds.service;

import com.sopds.domain.AuthUser;
import com.sopds.repository.AuthUserRepository;
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

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<AuthUser> getById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<AuthUser> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public AuthUser create(String username, String password, Boolean isSuperuser) {
        log.info("Creating user: {}", username);

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        AuthUser user = AuthUser.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .isSuperuser(isSuperuser != null && isSuperuser)
                .isStaff(isSuperuser != null && isSuperuser)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    public void updatePassword(Long userId, String newPassword) {
        log.info("Updating password for user ID: {}", userId);

        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean checkPassword(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    public void setActive(Long userId, Boolean active) {
        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsActive(active);
        userRepository.save(user);
    }

    public void setSuperuser(Long userId, Boolean isSuperuser) {
        AuthUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setIsSuperuser(isSuperuser);
        user.setIsStaff(isSuperuser);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<AuthUser> getActiveUsers() {
        return userRepository.findAllByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<AuthUser> getSuperusers() {
        return userRepository.findAllByIsSuperuserTrue();
    }

    public void delete(Long id) {
        log.info("Deleting user ID: {}", id);
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return userRepository.count();
    }
}
