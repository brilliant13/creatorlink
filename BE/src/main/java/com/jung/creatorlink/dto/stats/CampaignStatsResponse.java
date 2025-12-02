package com.jung.creatorlink.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CampaignStatsResponse {
    private Long campaignId;
    private String campaignName;
    private long totalClicks;
}
