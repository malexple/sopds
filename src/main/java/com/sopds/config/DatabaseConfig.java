package com.sopds.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
@Configuration
@ConditionalOnProperty(
        name = "spring.datasource.driver-class-name",
        havingValue = "org.sqlite.JDBC"
)
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Bean
    public DataSourceInitializer sqliteInitializer(DataSource dataSource) {
        return new DataSourceInitializer() {{
            setDataSource(dataSource);
            setDatabasePopulator(connection -> {
                try (Connection conn = dataSource.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA journal_mode=WAL");
                    stmt.execute("PRAGMA foreign_keys=ON");
                    stmt.execute("PRAGMA synchronous=NORMAL");
                    log.info("SQLite PRAGMA настройки применены");
                } catch (Exception e) {
                    log.warn("Не удалось применить SQLite PRAGMA: {}", e.getMessage());
                }
            });
        }};
    }


}