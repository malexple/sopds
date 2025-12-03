package com.sopds.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StatsDto {

    private long allbooks;
    private long allauthors;
    private long allgenres;
    private long allseries;
    private LocalDateTime lastscanDate;
}
