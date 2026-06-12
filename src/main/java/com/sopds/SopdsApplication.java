package com.sopds;

import com.sopds.config.SqliteDirectoryInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SopdsApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SopdsApplication.class);
        app.addListeners(new SqliteDirectoryInitializer());
        app.run(args);
    }
}