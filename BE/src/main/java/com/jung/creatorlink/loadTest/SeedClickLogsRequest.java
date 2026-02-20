package com.jung.creatorlink.loadTest;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SeedClickLogsRequest {
    //해당 캠페인의 ACTIVE tracking_link에만 분산
    private long campaignId;          // 어떤 캠페인 데이터로 넣을지 (tracking_links에서 campaign_id 필터) //클릭로그를 생성할 캠페인
    private int totalRows;            // 예: 10_000_000 // 생성할 총 클릭 로그 수
    private int batchSize = 5000;     // 5k~10k 정도 //1회 bulk INSERT 크기

    // 기간 분산 (daysBackFrom ~ daysBackTo)
    // 예: 90 ~ 30  => 최근 30~90일 사이로 분산
    private int daysBackFrom = 90; //클릭 날짜 범위 시작 (오늘 기준 90일 전)
    private int daysBackTo = 30; // 클릭 날짜 범위 끝 (오늘 기준 30일 전)

    // 편향 옵션 (현실적 쏠림 재현용)
    // 예: 0.2 => 20%는 인기 링크들(topK)에 몰아주기
    private double skewRatio = 0.2; //20%의 클릭을 인기 링크에 집중(현실적 쏠림 재현)
    private int hotLinkTopK = 200;    // 상위 K개 링크에 몰아줄지 //쏠림 대상 링크수(상위200개)
}

