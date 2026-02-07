package com.jung.creatorlink.loadTest;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SeedClickLogsRequest {
    private long campaignId;          // 어떤 캠페인 데이터로 넣을지 (tracking_links에서 campaign_id 필터)
    private int totalRows;            // 예: 10_000_000
    private int batchSize = 5000;     // 5k~10k 정도

    // 기간 분산 (daysBackFrom ~ daysBackTo)
    // 예: 90 ~ 30  => 최근 30~90일 사이로 분산
    private int daysBackFrom = 90;
    private int daysBackTo = 30;

    // 편향 옵션 (현실적 쏠림 재현용)
    // 예: 0.2 => 20%는 인기 링크들(topK)에 몰아주기
    private double skewRatio = 0.2;
    private int hotLinkTopK = 200;    // 상위 K개 링크에 몰아줄지
}

