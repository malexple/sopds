package com.sopds.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Создаёт директорию для SQLite БД ДО того как HikariCP попытается
 * открыть соединение. ApplicationEnvironmentPreparedEvent — самое
 * раннее событие Spring Boot, гарантированно до инициализации бинов.
 */
@Slf4j
public class SqliteDirectoryInitializer
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        String driverClass = env.getProperty("spring.datasource.driver-class-name", "");

        if (!"org.sqlite.JDBC".equals(driverClass)) {
            return; // не SQLite профиль — ничего не делаем
        }

        String url = env.getProperty("spring.datasource.url", "");
        String path = url.replaceFirst("^jdbc:sqlite:", "");

        // Разрешаем ${user.home} если Spring ещё не подставил
        path = path.replace("${user.home}", System.getProperty("user.home"));

        File dbDir = new File(path).getParentFile();
        if (dbDir != null && !dbDir.exists()) {
            boolean created = dbDir.mkdirs();
            if (created) {
                log.info("Создана директория для SQLite БД: {}", dbDir.getAbsolutePath());
            } else {
                log.warn("Не удалось создать директорию: {}", dbDir.getAbsolutePath());
            }
        }
    }
}