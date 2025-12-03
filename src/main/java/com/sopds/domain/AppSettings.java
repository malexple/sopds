package com.sopds.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", unique = true, nullable = false, length = 255)
    private String key;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "setting_type", length = 50)
    private String type;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getStringValue() {
        return value;
    }

    public Integer getIntValue() {
        return value != null ? Integer.parseInt(value) : null;
    }

    public Boolean getBooleanValue() {
        return value != null && Boolean.parseBoolean(value);
    }
}
