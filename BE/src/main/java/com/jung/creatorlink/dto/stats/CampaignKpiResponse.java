package com.jung.creatorlink.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CampaignKpiResponse {
    private long todayClicks;
    private long rangeClicks;
    private long totalClicks;
    private long activeLinks;
}
