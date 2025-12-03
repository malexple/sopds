package com.sopds.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "sopds")
@Getter
@Setter
public class SopdsProperties {

    private String rootLib = "/var/lib/sopds/books";
    private String bookExtensions = "fb2,epub,mobi,pdf";
    private boolean scanOnStartup = false;
    private int maxItems = 60;

    public List<String> getBookExtensionsList() {
        return Arrays.asList(bookExtensions.toLowerCase().split(","));
    }
}
