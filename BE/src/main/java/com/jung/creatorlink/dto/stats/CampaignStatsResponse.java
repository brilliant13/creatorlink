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
    // 전체 클릭 수
    private long totalClicks;

    // 오늘 클릭 수
    private long todayClicks;
}
