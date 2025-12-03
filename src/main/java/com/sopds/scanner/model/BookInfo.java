package com.sopds.scanner.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class BookInfo {

    private String title;
    private String annotation;
    private String language;
    private String docDate;

    @Builder.Default
    private List<AuthorInfo> authors = new ArrayList<>();

    @Builder.Default
    private List<GenreInfo> genres = new ArrayList<>();

    @Builder.Default
    private List<SeriesInfo> series = new ArrayList<>();

    // File info
    private String filename;
    private String path;
    private String format;
    private long filesize;

    // Archive info
    private boolean inArchive;
    private String archivePath;
}
