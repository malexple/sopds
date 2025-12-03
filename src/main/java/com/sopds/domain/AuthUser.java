package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_superuser", nullable = false)
    @Builder.Default
    private Boolean isSuperuser = false;

    @Column(name = "username", unique = true, nullable = false, length = 150)
    private String username;

    @Column(name = "first_name", length = 150)
    @Builder.Default
    private String firstName = "";

    @Column(name = "last_name", length = 150)
    @Builder.Default
    private String lastName = "";

    @Column(name = "email", length = 254)
    @Builder.Default
    private String email = "";

    @Column(name = "is_staff", nullable = false)
    @Builder.Default
    private Boolean isStaff = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "date_joined", nullable = false)
    private LocalDateTime dateJoined;

    @PrePersist
    protected void onCreate() {
        if (dateJoined == null) {
            dateJoined = LocalDateTime.now();
        }
    }
}
