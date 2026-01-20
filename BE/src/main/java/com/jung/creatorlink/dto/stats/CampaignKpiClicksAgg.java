package com.jung.creatorlink.dto.stats;

import lombok.Getter;

@Getter
public class CampaignKpiClicksAgg {
    private final long todayClicks;
    private final long rangeClicks;
    private final long totalClicks;

    // JPQL new(...) 타입 안전을 위해 Long으로 받고 내부에서 0 처리
    public CampaignKpiClicksAgg(Long todayClicks, Long rangeClicks, Long totalClicks) {
        this.todayClicks = todayClicks == null ? 0L : todayClicks;
        this.rangeClicks = rangeClicks == null ? 0L : rangeClicks;
        this.totalClicks = totalClicks == null ? 0L : totalClicks;
    }

    // (IDE/JPQL이 int로 추론하는 경우 대비)
//    public CampaignKpiClicksAgg(Integer todayClicks, Integer rangeClicks, Long totalClicks) {
//        this.todayClicks = todayClicks == null ? 0L : todayClicks.longValue();
//        this.rangeClicks = rangeClicks == null ? 0L : rangeClicks.longValue();
//        this.totalClicks = totalClicks == null ? 0L : totalClicks;
//    }
}
