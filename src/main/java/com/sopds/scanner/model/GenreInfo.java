package com.sopds.scanner.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreInfo {

    private String genre;
    private String section;
    private String subsection;
}
