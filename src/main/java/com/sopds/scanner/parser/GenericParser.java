package com.sopds.scanner.parser;

import com.sopds.scanner.model.AuthorInfo;
import com.sopds.scanner.model.BookInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class GenericParser implements BookParser {

    private static final Set<String> SUPPORTED_FORMATS = Set.of(
            "pdf", "djvu", "doc", "docx", "rtf", "txt", "mobi", "azw", "azw3"
    );

    @Override
    public boolean supports(String format) {
        return SUPPORTED_FORMATS.contains(format.toLowerCase());
    }

    @Override
    public BookInfo parse(InputStream inputStream, String filename) {
        // For formats without metadata, use filename as title
        String title = filename.replaceAll("\\.[^.]+$", "")
                .replace("_", " ")
                .replace("-", " ");

        return BookInfo.builder()
                .title(title)
                .authors(List.of(AuthorInfo.builder()
                        .lastName("Unknown")
                        .build()))
                .build();
    }
}
