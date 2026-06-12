package com.sopds.config;

import com.sopds.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminUserInitializer {

    private final UserService userService;

    @Bean
    public ApplicationRunner initDefaultAdmin() {
        return args -> {
            if (userService.count() == 0) {
                userService.create("admin", "admin", true);
                log.info("Default admin user created: admin/admin");
            }
        };
    }
}