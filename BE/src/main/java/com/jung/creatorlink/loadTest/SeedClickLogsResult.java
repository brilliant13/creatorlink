package com.jung.creatorlink.loadTest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class SeedClickLogsResult {
    private int inserted;
    private long elapsedMs;
}

