package com.sopds.scanner.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeriesInfo {

    private String name;
    private int number;
}
