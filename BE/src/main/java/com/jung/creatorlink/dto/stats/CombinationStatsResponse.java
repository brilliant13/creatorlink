package com.jung.creatorlink.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CombinationStatsResponse {
    private Long creatorId;
    private String creatorName;

    private Long channelId;
    private String channelDisplayName;

    private long todayClicks;
    private long rangeClicks;
    private long totalClicks;
}
